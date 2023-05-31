//
//  KronometerApp.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

@main
struct KronometerApp: App {
    @StateObject private var dataController = KronometerProvider.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(BLEController())
                .environmentObject(CountdownModel())
                .environmentObject(StartModel())
                .environment(\.managedObjectContext, dataController.container.viewContext)
       }
    }
}
