//
//  AuthServiceTests.swift
//  Kronometer
//
//  Created by Anze Staric on 18. 4. 25.
//

import XCTest

@testable import Kronometer

final class AuthServiceTests: XCTestCase {
    var mockSession: URLSession!
    var authService: AuthService!

    override func setUp() {
        super.setUp()
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockURLProtocol.self]
        mockSession = URLSession(configuration: config)
        authService = AuthService(session: mockSession)
    }

    override func tearDown() {
        mockSession = nil
        authService = nil
        super.tearDown()
    }
}

extension AuthServiceTests {
    func testValidAccessToken_WithValidStoreToken_ReturnsToken() async throws {
        let token = AccessToken(
            accessToken: "abc123",
            expires: Date().addingTimeInterval(3600),
            refreshToken: "refresh123"
        )
        authService.token = token

        let result = try await authService.validAccessToken()
        XCTAssertEqual(result?.accessToken, "abc123")
    }

    func testValidAccessToken_RefreshToken_Success() async throws {
        let responseJSON = """
            {
                "access_token": "new_token",
                "expires_in": 3600,
                "refresh_token": "new_refresh"
            }
            """.data(using: .utf8)!

        MockURLProtocol.requestHandler = { _ in
            let response = HTTPURLResponse(
                url: URL(string: "https://kronometer.staric.net")!,
                statusCode: 200,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, responseJSON)
        }

        let oldToken = AccessToken(
            accessToken: "old",
            expires: Date().addingTimeInterval(-100),  // expired
            refreshToken: "old_refresh"
        )
        authService.token = oldToken

        let token = try await authService.validAccessToken()
        XCTAssertEqual(token?.accessToken, "new_token")
    }

    func testValidAccessToken_RefreshToken_RevokedRefreshToken() async throws {
        let responseJSON = """
            {
                "error": "invalid_grant",
            }
            """.data(using: .utf8)!

        MockURLProtocol.requestHandler = { _ in
            let response = HTTPURLResponse(
                url: URL(string: "https://kronometer.staric.net")!,
                statusCode: 400,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, responseJSON)
        }

        let oldToken = AccessToken(
            accessToken: "old",
            expires: Date().addingTimeInterval(-100),  // expired
            refreshToken: "old_refresh"
        )
        authService.token = oldToken

        do {
            let _ = try await authService.validAccessToken()
            XCTFail()
        } catch {
            XCTAssertEqual(error as? ApiError, ApiError.serverError(400, "invalid_grant"))
        }
    }

    func testValidAccessToken_NoToken() async throws {
        authService.token = nil
        let token = try await authService.validAccessToken()
        XCTAssertEqual(token, nil)
    }
}

extension AuthServiceTests {
    func testLogin_SuccessfulAuthorization() async throws {
        let responseJSON = """
            {
                "access_token": "new_token",
                "expires_in": 3600,
                "refresh_token": "new_refresh"
            }
            """.data(using: .utf8)!

        MockURLProtocol.requestHandler = { _ in
            let response = HTTPURLResponse(
                url: URL(string: "https://kronometer.staric.net")!,
                statusCode: 200,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, responseJSON)
        }

        let token = try await authService.login { url, redirectUrlScheme in
            .init(string: "\(redirectUrlScheme)://auth/?code=code")!
        }
        XCTAssertEqual(token.accessToken, "new_token")
    }

    func testLogin_AccessDenied() async throws {
        do {
            let _ = try await authService.login { url, redirectUrlScheme in
                .init(string: "\(redirectUrlScheme)://auth/?error=access_denied")!
            }
            XCTFail()
        } catch {
            XCTAssertEqual(error as? ApiError, ApiError.serverError(nil, "access_denied"))
        }
    }

    func testLogin_LoginCancelled() async throws {
        do {
            let _ = try await authService.login { _, _ in
                throw NSError(
                    domain: "com.apple.AuthenticationServices.WebAuthenticationSession",
                    code: 1,
                    userInfo: nil
                )
            }
            XCTFail()
        } catch {
            XCTAssertEqual(error as? ApiError, ApiError.loginCancelled)
        }
    }
}

class MockURLProtocol: URLProtocol {
    static var requestHandler: ((URLRequest) throws -> (HTTPURLResponse, Data))?

    override class func canInit(with request: URLRequest) -> Bool { true }

    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }

    override func startLoading() {
        guard let handler = MockURLProtocol.requestHandler else {
            fatalError("Request handler not set.")
        }

        do {
            let (response, data) = try handler(request)
            client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            client?.urlProtocol(self, didLoad: data)
            client?.urlProtocolDidFinishLoading(self)
        } catch {
            client?.urlProtocol(self, didFailWithError: error)
        }
    }

    override func stopLoading() {}
}
