//
//  CountdownModel.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

@MainActor
final class CountdownModel: ObservableObject {
    @Published var counter: Int = 0
    @AppStorage("defaultCountdown") var defaultCountdown = 30
    private var endTime: Date?
    private var timer: Timer?

    var description: String {
        let seconds = String(format: "%02d", counter % 60)
        let minutes = String(format: "%02d", counter / 60)
        return "\(minutes):\(seconds)"
    }

    func reset() {
        endTime = Date() + Double(defaultCountdown)
        counter = defaultCountdown
        startTimer()
    }

    private func setCounter(to: Double) {
        counter = Int(to)
        endTime = Date() + to
    }

    @objc private func tick() {
        if let endTime = endTime {
            let newCounter = Int(Date().distance(to: endTime) + 1)
            if newCounter != counter {
                counter = max(newCounter, 0)
            }
            if counter <= 0 {
                stopTimer()
            }
        }
    }

    private func startTimer() {
        stopTimer()
        timer = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(tick), userInfo: nil, repeats: true)
    }

    private func stopTimer() {
        if let timer = timer {
            timer.invalidate()
        }
    }
}
