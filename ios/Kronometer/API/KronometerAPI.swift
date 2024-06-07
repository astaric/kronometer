//
//  KronometerAPI.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
//

import Foundation


class KronometerApi {
    private let bikerListUrl = URL(string:"https://kronometer.staric.net/biker/list")!
    private let setStartTimeUrl = URL(string: "https://kronometer.staric.net/biker/set_start_time")!
    private let setEndTimeUrl = URL(string: "https://kronometer.staric.net/biker/set_end_time")!

    static let shared = KronometerApi()

    func getBikers() async throws -> [BikerData] {
        let sesion = URLSession.shared
        guard let (data, response) = try? await sesion.data(from: bikerListUrl),
              let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200
        else {
            throw BikerError.fetchError
        }

        do {
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .formatted(DateFormatter.jsonUtc)
            let nodes = try decoder.decode([BikerNode].self, from: data)
            return nodes.map { $0.fields }
        } catch {
            throw BikerError.wrongDataFormat(error: error)
        }
    }

    func setStartTime(for bikerId: Int, to time: Date) async throws {
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

    func setEndTime(for bikerId: Int, to time: Date) async throws {
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
}
