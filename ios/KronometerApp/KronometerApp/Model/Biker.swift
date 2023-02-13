//
//  Biker.swift
//  KronometerApp
//
//  Created by Anze Staric on 27/01/2023.
//

import Foundation


struct Biker: Identifiable, Hashable, Codable {
    var id: Int
    var name: String

    var numberAndName: String {
        "\(id) \(name)"
    }

    var startTime: Date?
}
