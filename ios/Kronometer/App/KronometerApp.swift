//
//  KronometerApp.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

@main
struct KronometerApp: App {
    @State var countdown = CountdownViewModel()
    @State var bikerStore = BikerStore()
    @StateObject var sensortController = SensorController()

    var body: some Scene {
        WindowGroup {
            MainView()
                .environmentObject(sensortController)
                .environment(countdown)
                .environment(bikerStore)
        }
    }
}
