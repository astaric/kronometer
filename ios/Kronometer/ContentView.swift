//
//  ContentView.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        BTSensors()
        StartHome()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(CountdownModel())
            .environmentObject(BLEController())
    }
}
