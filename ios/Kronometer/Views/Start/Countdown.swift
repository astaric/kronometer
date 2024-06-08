//
//  Countdown.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//
import SwiftUI

struct Countdown: View {
    @Environment(CountdownModel.self) var countdown: CountdownModel


    var body: some View {
        if countdown.active {
            TimelineView(.animation(minimumInterval: 0.1)) { timeline in
                countdownText
            }
        } else {
            countdownText
        }
    }
    
    var countdownText: some View {
        Text("\(countdown.description)")
            .font(.custom("", size: 100, relativeTo: .largeTitle))
    }
}

struct Countdown_Previews: PreviewProvider {
    static var previews: some View {
        let model = CountdownModel()
        model.reset()
        return Countdown()
            .environment(model)
    }
}
