//
//  ContentView.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        NavigationView {
            VStack {
                //DebugBikerSync()
                StartHome()
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink(destination: DebugBTSensors()) {
                        Image("Bluetooth")
                            .colorInvert()
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
