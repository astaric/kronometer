//
//  KronometerAPI.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
//

import AuthenticationServices
import CryptoKit
import SwiftUI

class ApiService {
    static let shared = ApiService()

    private let session: URLSession
    init(session: URLSession = .shared) {
        self.session = session
    }
}

extension ApiService {
    static let baseUrl = URL(string: "https://kronometer.staric.net")!

    func makeRequest(
        method: String,
        path: String,
        parameters: [URLQueryItem]? = nil,
        accessToken: String
    ) async throws -> Data {
        var components = URLComponents(url: Self.baseUrl, resolvingAgainstBaseURL: true)!
        components.path = path
        if let parameters {
            components.queryItems = parameters
        }

        var request: URLRequest
        switch method {
        case "GET":
            request = URLRequest(url: components.url!)

        case "POST":
            let body = components.query
            components.queryItems = nil

            request = URLRequest(url: components.url!)
            request.httpMethod = "POST"
            request.httpBody = body?.data(using: .utf8)

        default:
            throw ApiError.invalidRequest("Unsupported httpMethod \(method)")
        }

        request.addValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")

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

        return data
    }

    struct ErrorResponse: Decodable {
        let error: String
    }
}

extension ApiService {
    func getCompetitions(accessToken: String) async throws -> [Competition] {
        let data = try await makeRequest(
            method: "GET", path: "/api/competition/", accessToken: accessToken)

        let decoder = JSONDecoder()
        return try decoder.decode(CompetitionApiResponse.self, from: data).competitions
    }

    struct Competition: Decodable, Hashable, Identifiable {
        let id: Int
        let name: String
        let archived: Bool
    }

    private struct CompetitionApiResponse: Decodable {
        let competitions: [Competition]
    }
}

extension ApiService {
    func getBikers(competitionId: Int, accessToken: String) async throws -> [Biker] {
        let data = try await makeRequest(
            method: "GET", path: "/api/competition/\(competitionId)/biker/",
            accessToken: accessToken)

        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601withFractionalSeconds
        return try decoder.decode(BikerApiResponse.self, from: data).bikers
    }

    struct Biker: Decodable {
        let competition_id: Int
        let number: Int
        let name: String
        let surname: String
        let start_time: Date?
        let end_time: Date?
    }

    private struct BikerApiResponse: Decodable {
        let bikers: [Biker]
    }

}
extension ApiService {
    func updateTimes(
        competitionId: Int, bikerNumber: Int, startTime: Date? = nil, endTime: Date? = nil,
        accessToken: String
    ) async throws {
        var parameters = [URLQueryItem]()
        if let startTime {
            parameters.append(
                .init(
                    name: "start_time", value: String(Int(startTime.timeIntervalSince1970 * 1000)))
            )
        }
        if let endTime {
            parameters.append(
                .init(name: "end_time", value: String(Int(endTime.timeIntervalSince1970 * 1000)))
            )
        }

        let _ = try await makeRequest(
            method: "POST",
            path: "/api/competition/\(competitionId)/biker/\(bikerNumber)/",
            parameters: parameters,
            accessToken: accessToken
        )
    }
}
