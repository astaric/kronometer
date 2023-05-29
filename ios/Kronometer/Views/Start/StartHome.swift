//
//  StartHome.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

struct StartHome: View {
    @Environment(\.verticalSizeClass) var verticalSizeClass: UserInterfaceSizeClass?

    var body: some View {
        if verticalSizeClass == .regular {
            VStack {
                Countdown()
                    .padding([.top, .bottom], 50)
                Separator()
                StartButton()
                    .ignoresSafeArea()
            }
        } else {
            HStack {
                VStack() {
                    Countdown()
                    Spacer()
                    Separator()
                    Spacer()

                }.padding(50)
                StartButton()
            }
            .ignoresSafeArea()
        }
    }
}

struct StartHome_Previews: PreviewProvider {
    static var previews: some View {
        let countdownModel = CountdownModel()
        countdownModel.reset()
        return StartHome()
            .environmentObject(countdownModel)
    }
}
