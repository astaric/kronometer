//
//  KronometerAPI.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
//

import Foundation


class KronometerApi {
    private static let bikerListUrl = URL(string:"https://kronometer.staric.net/biker/list")!
    private static let setStartTimeUrl = URL(string: "https://kronometer.staric.net/biker/set_start_time")!
    private static let setEndTimeUrl = URL(string: "https://kronometer.staric.net/biker/set_end_time")!

    static func getBikers() async throws -> [BikerData] {
        let sesion = URLSession.shared
        guard let (data, response) = try? await sesion.data(from: bikerListUrl),
              let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200
        else {
            throw BikerError.fetchError
        }

        return try parseBikers(data: data)
    }
    
    static func parseBikers(data: Data) throws -> [BikerData] {
        do {
            let decoder = JSONDecoder()
            let nodes = try decoder.decode([BikerNode].self, from: data)
            return nodes.map { $0.fields }
        } catch {
            print(error)
            throw BikerError.wrongDataFormat(error: error)
        }
    }

    static func setStartTime(for bikerId: Int, to time: Date) async throws {
        var urlComponents = URLComponents(url: setStartTimeUrl, resolvingAgainstBaseURL: false)!
        urlComponents.queryItems = [
            URLQueryItem(name: "number", value: "\(bikerId)"),
            URLQueryItem(name: "start_time", value: "\(Int(time.timeIntervalSince1970 * 1000))")

        ]
        let response: URLResponse
        let data: Data
        do {
            (data, response) = try await URLSession.shared.data(from: urlComponents.url!)
        } catch {
            throw BikerError.pushError(error: error)
        }
        guard
            let response = response as? HTTPURLResponse,
            (200...299).contains(response.statusCode) else {
            throw BikerError.serverError(error: String(data: data, encoding: .utf8) ?? "unknown error")
        }
    }

    static func setEndTime(for bikerId: Int, to time: Date) async throws {
        var urlComponents = URLComponents(url: setEndTimeUrl, resolvingAgainstBaseURL: false)!
        urlComponents.queryItems = [
            URLQueryItem(name: "number", value: "\(bikerId)"),
            URLQueryItem(name: "end_time", value: "\(Int(time.timeIntervalSince1970 * 1000))")
        ]
        let response: URLResponse
        let data: Data
        do {
            (data, response) = try await URLSession.shared.data(from: urlComponents.url!)
        } catch {
            throw BikerError.pushError(error: error)
        }
        guard
            let response = response as? HTTPURLResponse,
            (200...299).contains(response.statusCode) else {
            throw BikerError.serverError(error: String(data: data, encoding: .utf8) ?? "unknown error")
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

    var dictionaryValue: [String: Any] {
        [
            "number": number,
            "name": "\(name) \(surname)",
            "start_time": start_time as Any
        ]
    }
    
    private enum CodingKeys : String, CodingKey { case number, name, surname, start_time }
    private static let dateFormatter = ISO8601DateFormatter()
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        number = try container.decode(Int.self, forKey: .number)
        name = try container.decode(String.self, forKey: .name)
        surname = try container.decode(String.self, forKey: .surname)
        
        if var rawDate = try? container.decode(String?.self, forKey: .start_time) {
            rawDate.replace(/\.\d+/, with: "")
            start_time = Self.dateFormatter.date(from: rawDate)
        } else {
            start_time = nil
        }
        
    }
}

enum BikerError: Error {
    case batchInsertError
    case fetchError
    case pushError(error: Error)
    case serverError(error: String)
    case wrongDataFormat(error: Error)
}

extension BikerError: LocalizedError {
    var errorDescription: String? {
        switch self {
            case .batchInsertError:
                return NSLocalizedString("Could not execute batch insert request", comment: "")
            case .fetchError:
                return NSLocalizedString("Could not download biker data", comment: "")
            case .pushError(let error):
                return NSLocalizedString("Could not upload data: \(error.localizedDescription)", comment: "")
            case .serverError(let error):
                return NSLocalizedString("Server returned error: \(error)", comment: "")
            case .wrongDataFormat(let error):
                return NSLocalizedString("Could not parse json data: \(error.localizedDescription)", comment: "")
        }
    }
}
