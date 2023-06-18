//
//  ContentView.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

enum AppMode: String {
    case start = "start"
    case finish = "finish"
}

struct ContentView: View {
    @AppStorage("debug")
    var debug = false
    @AppStorage("mode")
    var mode = ""

    var appMode: AppMode {
        AppMode(rawValue: mode) ?? AppMode.start
    }

    var body: some View {
        NavigationStack {
            VStack {
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
        ContentView()
            .environmentObject(CountdownModel())
            .environmentObject(BLEController())
    }
}
