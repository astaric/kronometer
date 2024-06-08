//
//  Biker.swift
//  Kronometer
//
//  Created by Anze Staric on 7. 6. 24.
//

import Foundation


struct Biker: Hashable, Identifiable {
    var id: Int
    var name: String
    var startTime: Date?
    var endTime: Date?
}
