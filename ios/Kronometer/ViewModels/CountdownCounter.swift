//
//  CountdownModel.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

@Observable
class CountdownCounter {
    var defaultCountdown: Int 
    {
        didSet {
            UserDefaults.standard.setValue(defaultCountdown, forKey: "defaultCountdown")
        }
    }
    
    init() {
        var defaultCountdown = UserDefaults.standard.integer(forKey: "defaultCountdown")
        if defaultCountdown == 0 {
            defaultCountdown = 30
        }
        self.defaultCountdown = defaultCountdown
    }
    
    var endTime: Date?
    var active = false
    
    var timeRemaining: Double {
        if let endTime {
            let timeRemaining = endTime.timeIntervalSince(Date())
            return timeRemaining > 0 ? timeRemaining : 0
        } else {
            return 0
        }
    }

    var description: String {
        if timeRemaining <= 0 {
            active = false
        }
        let seconds = String(format: "%02d", Int(timeRemaining.truncatingRemainder(dividingBy: 60)))
        let minutes = String(format: "%02d", Int(timeRemaining / 60))
        return "\(minutes):\(seconds)"
    }

    func reset() {
        endTime = Date() + Double(defaultCountdown) + 1
        active = true
    }
}
