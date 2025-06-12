//
//  AuthService.swift
//  Kronometer
//
//  Created by Anze Staric on 15. 4. 25.
//

import CryptoKit
import Foundation

struct AccessToken: Codable, Equatable {
    var accessToken: String
    var expires: Date
    var refreshToken: String

    var willExpireSoon: Bool {
        expires.timeIntervalSinceNow < 5 * 60
    }
}

class AuthService {
    typealias AuthenticateHandler = (URL, String) async throws -> URL

    let baseUrl = URL(string: "https://kronometer.staric.net")!

    @KeychainBacked(key: "net.staric.kronometer.access_token")
    var token: AccessToken?

    static let shared = AuthService()

    private let session: URLSession
    init(session: URLSession = .shared) {
        self.session = session
    }

    // MARK: - Public Methods

    func validAccessToken() async throws -> AccessToken? {
        guard var currentToken = token else { return nil }
        if currentToken.willExpireSoon {
            currentToken = try await refresh(token: currentToken)
            token = currentToken
        }
        return currentToken
    }

    func login(
        authenticateHandler: @escaping AuthenticateHandler
    ) async throws -> AccessToken {
        let codeVerifier = generateCodeVerifier()
        let codeChallenge = generateCodeChallenge(from: codeVerifier)
        let redirectURI = "\(Constants.callbackURLScheme)://auth"
        let authorizeURL = try buildOAuthAuthorizeUrl(
            codeChallenge: codeChallenge,
            redirectURI: redirectURI
        )

        var callbackURL: URL
        do {
            callbackURL = try await authenticateHandler(
                authorizeURL,
                Constants.callbackURLScheme
            )
        } catch {
            print(error)
            throw ApiError.loginCancelled
        }
        let code = try parseAuthorizationCode(from: callbackURL)

        return try await getOAuthToken(
            code: code, codeVerifier: codeVerifier, redirectURI: redirectURI)
    }

    func refresh(token: AccessToken) async throws -> AccessToken {
        return try await getOAuthToken(refreshToken: token.refreshToken)
    }

    func revoke(token: AccessToken) async {
        for token in [token.accessToken, token.refreshToken] {
            try? await revokeOAuthToken(token: token)
        }
    }

    // MARK: - Private Helpers

    private var clientId: String? {
        Bundle.main.object(forInfoDictionaryKey: "CLIENT_ID") as? String
    }
    private var clientSecret: String? {
        Bundle.main.object(forInfoDictionaryKey: "CLIENT_SECRET") as? String
    }

    private func buildOAuthAuthorizeUrl(
        codeChallenge: String,
        redirectURI: String
    ) throws -> URL {
        var components = URLComponents(
            url: baseUrl,
            resolvingAgainstBaseURL: true
        )
        components?.path = Constants.authorizePath
        components?.queryItems = [
            URLQueryItem(name: "response_type", value: "code"),
            URLQueryItem(name: "code_challenge", value: codeChallenge),
            URLQueryItem(name: "code_challenge_method", value: "S256"),
            URLQueryItem(name: "client_id", value: clientId),
            URLQueryItem(name: "redirect_uri", value: redirectURI),
        ]

        guard let authorizeURL = components?.url else {
            throw ApiError.invalidRequest("Failed to construct authorize URL")
        }

        return authorizeURL
    }

    private func parseAuthorizationCode(from callbackURL: URL) throws -> String {
        let queryItems = URLComponents(string: callbackURL.absoluteString)?
            .queryItems
        if let code = queryItems?.first(where: { $0.name == "code" })?.value {
            return code
        }
        if let error = queryItems?.first(where: { $0.name == "error" })?.value {
            throw ApiError.serverError(nil, error)
        }
        throw ApiError.serverError(nil, "Invalid callbackURL")
    }

    private func generateCodeVerifier() -> String {
        let charset = Array(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        )
        let length = Int.random(in: 43...128)
        return String(
            (0..<length).map { _ in charset.randomElement()! }
        )
    }

    private func generateCodeChallenge(from codeVerifier: String) -> String {
        let hash = SHA256.hash(data: Data(codeVerifier.utf8))
        return Data(hash).base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")  // Remove padding
    }

    private func getOAuthToken(
        code: String,
        codeVerifier: String,
        redirectURI: String,
    ) async throws -> AccessToken {
        return try await getOAuthToken([
            .init(name: "grant_type", value: "authorization_code"),
            .init(name: "code", value: code),
            .init(name: "client_id", value: clientId),
            .init(name: "client_secret", value: clientSecret),
            .init(name: "code_verifier", value: codeVerifier),
            .init(name: "redirect_uri", value: redirectURI),
        ])
    }

    private func getOAuthToken(
        refreshToken: String
    ) async throws -> AccessToken {
        return try await getOAuthToken([
            .init(name: "grant_type", value: "refresh_token"),
            .init(name: "client_id", value: clientId),
            .init(name: "client_secret", value: clientSecret),
            .init(name: "refresh_token", value: refreshToken),
        ])
    }

    private func getOAuthToken(
        _ body: [URLQueryItem]
    ) async throws -> AccessToken {
        let request = makePOSTRequest(path: Constants.tokenPath, body: body)
        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw ApiError.invalidResponse("No HTTP response received")
        }
        guard (200...299).contains(httpResponse.statusCode) else {
            let decoder = JSONDecoder()

            if let errorMessage = try? decoder.decode(ErrorResponse.self, from: data).error {
                throw ApiError.serverError(httpResponse.statusCode, errorMessage)
            } else if let responseString = String(data: data, encoding: .utf8) {
                throw ApiError.serverError(httpResponse.statusCode, responseString)
            } else {
                throw ApiError.serverError(
                    httpResponse.statusCode, "Server error: \(httpResponse.statusCode)")
            }
        }

        guard
            let json = try JSONSerialization.jsonObject(with: data)
                as? [String: Any],
            let accessToken = json["access_token"] as? String,
            let expiresIn = json["expires_in"] as? Double,
            let refreshToken = json["refresh_token"] as? String
        else {
            throw ApiError.invalidResponse(
                "Invalid token data \(String(data: data, encoding: .utf8) ?? "No data)")")
        }

        let token = AccessToken(
            accessToken: accessToken,
            expires: Date(timeIntervalSinceNow: expiresIn),
            refreshToken: refreshToken
        )
        return token
    }

    struct ErrorResponse: Decodable {
        let error: String
    }

    private func revokeOAuthToken(token: String) async throws {
        let request = makePOSTRequest(
            path: Constants.revokePath,
            body: [
                .init(name: "token", value: token),
                .init(name: "client_id", value: clientId),
                .init(name: "client_secret", value: clientSecret),
            ]
        )
        let (_, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw ApiError.invalidResponse("No HTTP response received")
        }
        guard httpResponse.statusCode == 200 else {
            throw ApiError.invalidResponse("Unexpected response from server")
        }
    }

    private func makePOSTRequest(path: String, body: [URLQueryItem])
        -> URLRequest
    {
        var components = URLComponents()
        components.queryItems = body

        var request = URLRequest(url: URL(string: path, relativeTo: baseUrl)!)
        request.httpMethod = "POST"
        request.setValue(
            "application/x-www-form-urlencoded",
            forHTTPHeaderField: "Content-Type"
        )
        request.httpBody = components.query?.data(using: .utf8)
        return request
    }

    private enum Constants {
        static let callbackURLScheme = "x-kronometer-app"
        static let authorizePath = "/oauth/authorize/"
        static let tokenPath = "/oauth/token/"
        static let revokePath = "/oauth/revoke_token/"
    }
}
