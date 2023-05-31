//
//  StartButton.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

struct StartButton: View {
    @EnvironmentObject var bleController: BLEController
    @EnvironmentObject var countdown: CountdownModel
    @EnvironmentObject var startModel: StartModel
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        Button {
            start()
        } label: {
            Text("Start")
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .font(.custom("", size: 50, relativeTo: .largeTitle))
                .foregroundColor(.primary)
        }
        .background(colorScheme == .light ? Color.light : Color.dark)
        .onReceive(self.bleController.$events) { newValue in
            if countdown.counter <= 5 {
                start()
            }
        }
        .onChange(of: self.countdown.defaultCountdown) { newValue in
            countdown.reset()
        }
    }

    private func start() {
        startModel.startBiker()
        countdown.reset()
    }
}

struct StartButton_Previews: PreviewProvider {
    static var previews: some View {
        StartButton()
            .environmentObject(CountdownModel())
    }
}
