//
//  Countdown.swift
//  KronometerApp
//
//  Created by Anze Staric on 27/01/2023.
//

import SwiftUI

struct Countdown: View {
    @EnvironmentObject var countdown: CountdownModel


    var body: some View {
        Text(countdown.description)
            .font(.custom("test", size: 100, relativeTo: .largeTitle))

    }
}

struct Countdown_Previews: PreviewProvider {
    static var previews: some View {
        Countdown()
            .environmentObject(CountdownModel())
    }
}
