//
//  Biker.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import Foundation

struct BikerNode: Decodable {
    let fields: BikerProperties
}

struct BikerProperties: Decodable {
    let number: Int
    let name: String
    let surname: String

    var dictionaryValue: [String: Any] {
        [
            "number": number,
            "name": "\(name) \(surname)"
        ]
    }
}

func parseBikersJson(jsonData: Data) throws -> [BikerProperties] {
    let nodes = try JSONDecoder().decode([BikerNode].self, from: jsonData)
    return nodes.map { $0.fields }
}
