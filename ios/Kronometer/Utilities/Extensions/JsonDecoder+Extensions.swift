//
//  JsonDecoder+Extensions.swift
//  Kronometer
//
//  Created by Anze Staric on 17. 4. 25.
//
import Foundation

extension JSONDecoder.DateDecodingStrategy {
    static let iso8601withFractionalSeconds = custom { decoder in
        let container = try decoder.singleValueContainer()
        let dateString = try container.decode(String.self)
        // Use the built-in Date.ISO8601FormatStyle with fractional seconds
        let format = Date.ISO8601FormatStyle(includingFractionalSeconds: true)
        if let date = try? Date(dateString, strategy: format) {
            return date
        }
        throw DecodingError.dataCorruptedError(in: container, debugDescription: String(localized: "invalid_date", defaultValue: "Invalid date: \(dateString)"))
    }
}

