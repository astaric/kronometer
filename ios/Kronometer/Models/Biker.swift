//
//  Biker.swift
//  Kronometer
//
//  Created by Anze Staric on 7. 6. 24.
//

import Foundation


struct Biker: Codable, Hashable, Identifiable {
    var id: Int
    var name: String
    var startTime: Date?
    var arrivedOnFinish: Date?
    var endTime: Date?
}
