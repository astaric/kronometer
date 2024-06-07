//
//  MainView.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

enum AppMode: String {
    case start, finish
}

struct MainView: View {
    @AppStorage("debug")
    var debug = false
    @AppStorage("appMode")
    var appMode: AppMode = .start

    var body: some View {
        NavigationStack {
            VStack {
                HStack {
                    Image(systemName: "line.3.horizontal")
                    Spacer()
                    Image("Bluetooth")
                }
                
                switch appMode {
                    case .start:
                        StartHome()
                    case .finish:
                        FinishHome()
                }
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink(destination: SensorSettings()) {
                        Image("Bluetooth")
                            .foregroundColor(.primary)
                    }
                }
                if debug {
                    ToolbarItem(placement: .navigationBarLeading) {
                        NavigationLink(destination: DebugMenu()) {
                            Image(systemName: "line.3.horizontal")
                                .foregroundColor(.primary)
                        }
                    }
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
            .environmentObject(CountdownModel())
            .environmentObject(BLEController())
    }
}
