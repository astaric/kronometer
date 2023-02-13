//
//  StartHome.swift
//  KronometerApp
//
//  Created by Anze Staric on 27/01/2023.
//

import SwiftUI

struct StartHome: View {
    @EnvironmentObject var modelData: StartModel
    @Environment(\.verticalSizeClass) var verticalSizeClass: UserInterfaceSizeClass?

    var body: some View {
        if verticalSizeClass == .regular {
            VStack {
                Countdown()
                    .padding([.top, .bottom], 50)
                Separator()
                CurrentBiker()
                    .padding([.top, .bottom], 50)
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
                    CurrentBiker()


                }.padding(50)
                StartButton()
            }
            .ignoresSafeArea()
        }
    }
}

struct StartHome_Previews: PreviewProvider {
    static var previews: some View {
        StartHome()
            .environmentObject(StartModel())
            .environmentObject(CountdownModel())
    }
}
