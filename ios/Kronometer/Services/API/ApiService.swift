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
    let baseUrl = URL(string: "https://kronometer.staric.net")!
    
    static let shared = ApiService()
    
    private let session: URLSession
    init(session: URLSession = .shared) {
        self.session = session
    }
}


extension ApiService {
    func getCompetitions(accessToken: String) async throws -> [Competition] {
        var request = URLRequest(url: baseUrl.appendingPathComponent("/api/competition/"))
        request.addValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        
        guard let (data, response) = try? await session.data(for: request),
              let httpResponse = response as? HTTPURLResponse
        else {
            throw ApiError.fetchError
        }
        
        guard httpResponse.statusCode == 200
        else {
            let message = String(data: data, encoding: .utf8)
            throw ApiError.invalidResponse("\(httpResponse.statusCode) \(message ?? "")")
        }
        
        do {
            let decoder = JSONDecoder()
            return try decoder.decode(CompetitionApiResponse.self, from: data).competitions
        } catch {
            throw ApiError.wrongDataFormat(error: error)
        }
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
        var request = URLRequest(url: baseUrl.appendingPathComponent("/api/competition/\(competitionId)/biker/"))
        request.addValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        
        guard let (data, response) = try? await session.data(for: request),
              let httpResponse = response as? HTTPURLResponse
        else {
            throw ApiError.fetchError
        }
                
        guard httpResponse.statusCode == 200
        else {
            let message = String(data: data, encoding: .utf8)
            throw ApiError.invalidResponse("\(httpResponse.statusCode) \(message ?? "")")
        }

        do {
            let decoder = JSONDecoder()
            return try decoder.decode(BikerApiResponse.self, from: data).bikers
        } catch {
            throw ApiError.wrongDataFormat(error: error)
        }
    }
    
    struct Biker: Decodable {
        let number: Int
        let name: String
        let surname: String
        let start_time: Date?
        let end_time: Date?

        private enum CodingKeys: String, CodingKey {
            case number, name, surname, start_time, end_time
        }
        private static let dateFormatter = ISO8601DateFormatter()

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            number = try container.decode(Int.self, forKey: .number)
            name = try container.decode(String.self, forKey: .name)
            surname = try container.decode(String.self, forKey: .surname)

            if var rawDate = try? container.decode(
                String?.self,
                forKey: .start_time
            ) {
                rawDate.replace(/\.\d+/, with: "")
                start_time = Self.dateFormatter.date(from: rawDate)
            } else {
                start_time = nil
            }
            if var rawDate = try? container.decode(String?.self, forKey: .end_time)
            {
                rawDate.replace(/\.\d+/, with: "")
                end_time = Self.dateFormatter.date(from: rawDate)
            } else {
                end_time = nil
            }

        }
    }
    
    private struct BikerApiResponse: Decodable {
        let bikers: [Biker]
    }


    func setStartTime(for bikerId: Int, to time: Date) async throws {
    }

    func setEndTime(for bikerId: Int, to time: Date) async throws {
    }
}

@MainActor
class ApiManager2 {
    let baseUrl = URL(string: "https://kronometer.staric.net")!
    static let shared = ApiManager()

    private var _token: AccessToken?
    
    private let session: URLSession
    init(session: URLSession = .shared) {
        self.session = session
    }
}


extension ApiManager2 {
    
    private var bikerListUrl: URL { URL(string: "\(baseUrl)/biker/list")! }
    private var competitionListUrl: URL {
        URL(string: "competition/list", relativeTo: baseUrl)!
    }
    private var setStartTimeUrl: URL {
        URL(string: "biker/set_start_time", relativeTo: baseUrl)!
    }
    private var setEndTimeUrl: URL {
        URL(string: "biker/set_end_time", relativeTo: baseUrl)!
    }

    func getBikers() async throws -> [BikerData] {
        guard
            let (data, response) = try? await session.data(
                from: bikerListUrl
            ),
            let httpResponse = response as? HTTPURLResponse,
            httpResponse.statusCode == 200
        else {
            throw ApiError.fetchError
        }

        return try parseBikers(data: data)
    }

    func parseBikers(data: Data) throws -> [BikerData] {
        do {
            let decoder = JSONDecoder()
            let nodes = try decoder.decode([BikerNode].self, from: data)
            return nodes.map { $0.fields }
        } catch {
            throw ApiError.wrongDataFormat(error: error)
        }
    }

    func setStartTime(for bikerId: Int, to time: Date) async throws {
        var urlComponents = URLComponents(
            url: setStartTimeUrl,
            resolvingAgainstBaseURL: false
        )!
        urlComponents.queryItems = [
            URLQueryItem(name: "number", value: "\(bikerId)"),
            URLQueryItem(
                name: "start_time",
                value: "\(Int(time.timeIntervalSince1970 * 1000))"
            ),

        ]
        let response: URLResponse
        let data: Data
        do {
            (data, response) = try await session.data(
                from: urlComponents.url!
            )
        } catch {
            throw ApiError.pushError(error: error)
        }
        guard
            let response = response as? HTTPURLResponse,
            (200...299).contains(response.statusCode)
        else {
            throw ApiError.serverError(
                error: String(data: data, encoding: .utf8) ?? "unknown error"
            )
        }
    }

    func setEndTime(for bikerId: Int, to time: Date) async throws {
        var urlComponents = URLComponents(
            url: setEndTimeUrl,
            resolvingAgainstBaseURL: false
        )!
        urlComponents.queryItems = [
            URLQueryItem(name: "number", value: "\(bikerId)"),
            URLQueryItem(
                name: "end_time",
                value: "\(Int(time.timeIntervalSince1970 * 1000))"
            ),
        ]
        let response: URLResponse
        let data: Data
        do {
            (data, response) = try await session.data(
                from: urlComponents.url!
            )
        } catch {
            throw ApiError.pushError(error: error)
        }
        guard
            let response = response as? HTTPURLResponse,
            (200...299).contains(response.statusCode)
        else {
            throw ApiError.serverError(
                error: String(data: data, encoding: .utf8) ?? "unknown error"
            )
        }
    }
}

struct BikerNode: Decodable {
    let fields: BikerData
}

struct BikerData: Decodable {
    let number: Int
    let name: String
    let surname: String
    let start_time: Date?
    let end_time: Date?

    private enum CodingKeys: String, CodingKey {
        case number, name, surname, start_time, end_time
    }
    private static let dateFormatter = ISO8601DateFormatter()

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        number = try container.decode(Int.self, forKey: .number)
        name = try container.decode(String.self, forKey: .name)
        surname = try container.decode(String.self, forKey: .surname)

        if var rawDate = try? container.decode(
            String?.self,
            forKey: .start_time
        ) {
            rawDate.replace(/\.\d+/, with: "")
            start_time = Self.dateFormatter.date(from: rawDate)
        } else {
            start_time = nil
        }
        if var rawDate = try? container.decode(String?.self, forKey: .end_time)
        {
            rawDate.replace(/\.\d+/, with: "")
            end_time = Self.dateFormatter.date(from: rawDate)
        } else {
            end_time = nil
        }

    }
}

struct CompetitionNode: Decodable {
    let pk: Int
    let fields: CompetitionData
}

struct CompetitionData: Decodable {
    var id: Int?
    let title: String
}

enum ApiError: Error {
    case tokenExchangeError(_ error: Error)
    case invalidRequest(_ message: String)
    case invalidResponse(_ message: String)
    case invalidRefreshToken

    case noCompetitionSelected
    case batchInsertError
    case fetchError
    case pushError(error: Error)
    case serverError(error: String)
    case wrongDataFormat(error: Error)
}

extension ApiError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .noCompetitionSelected:
            return "Please select competition"            
        case .tokenExchangeError(let error):
            return "Could not exchange token: \(error.localizedDescription)"
        case .invalidRequest(let message):
            return "Invalid request: \(message)"
        case .invalidResponse(let message):
            return "Invalid server response: \(message)"
        case .invalidRefreshToken:
            return "Refresh token is invalid"
        case .batchInsertError:
            return "Could not execute batch insert request"
        case .fetchError:
            return "Could not download biker data"
        case .pushError(let error):
            return "Could not upload data: \(error.localizedDescription)"
        case .serverError(let error):
            return "Server returned error: \(error)"
        case .wrongDataFormat(let error):
            return "Could not parse JSON data: \(error.localizedDescription)"
        }
    }
}
