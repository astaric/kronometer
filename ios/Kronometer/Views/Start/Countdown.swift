//
//  Countdown.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//
import SwiftUI

struct Countdown: View {
    @EnvironmentObject var countdown: CountdownModel


    var body: some View {
        Text(countdown.description)
            .font(.custom("", size: 100, relativeTo: .largeTitle))

    }
}

struct Countdown_Previews: PreviewProvider {
    static var previews: some View {
        let model = CountdownModel()
        model.reset()
        return Countdown()
            .environmentObject(model)
    }
}
