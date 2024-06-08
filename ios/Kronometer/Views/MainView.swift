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
                StartHome()
//                switch appMode {
//                    case .start:
//                        StartHome()
//                    case .finish:
//                        //FinishHome()
//                        break;
//                }
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Menu {
                        Button("Start") {
                            appMode = .start
                        }
                        Button("Finish") {
                            appMode = .finish
                        }
                        NavigationLink(destination: DebugMenu()) {
                            Text("Debug")
                        }
                    } label: {
                        Image(systemName: "line.3.horizontal")
                            .foregroundColor(.primary)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink(destination: SensorSettings()) {
                        BluetoothStatus()
                    }
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
            .environment(CountdownModel())
            .environment(BikerStore())
            .environmentObject(BLEController())
    }
}
