//
//  StartButton.swift
//  KronometerApp
//
//  Created by Anze Staric on 27/01/2023.
//

import SwiftUI

struct StartButton: View {
    @EnvironmentObject var startModel: StartModel
    @EnvironmentObject var countdown: CountdownModel
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        Button {
            startModel.startBiker()
            countdown.reset()

        } label: {
            Text("Start")
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .font(.custom("", size: 50, relativeTo: .largeTitle))
                .foregroundColor(.primary)

        }
        .background(colorScheme == .light ? Color.light : Color.dark)
    }
}

struct StartButton_Previews: PreviewProvider {
    static var previews: some View {
        StartButton()
            .environmentObject(CountdownModel())
    }
}
