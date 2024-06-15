//
//  Countdown.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//
import SwiftUI

struct Countdown: View {
    @Environment(CountdownCounter.self) var countdown

    var body: some View {
        ZStack {
            if countdown.active {
                TimelineView(.animation(minimumInterval: 0.1)) { timeline in
                    countdownText(countdown.description)
                }
            } else {
                countdownText("00:00")
            }
        }
    }
    
    func countdownText(_ text: String) -> some View {
        Text(text)
            .font(.custom("", size: 100, relativeTo: .largeTitle))
    }
}

struct Countdown_Previews: PreviewProvider {
    static var previews: some View {
        return Countdown()
            .environment(CountdownCounter())
    }
}
