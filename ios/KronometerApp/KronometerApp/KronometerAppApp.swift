//
//  KronometerAppApp.swift
//  KronometerApp
//
//  Created by Anze Staric on 11/01/2023.
//

import SwiftUI

@main
struct KronometerAppApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(StartModel())
                .environmentObject(CountdownModel())
        }
    }
}
