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
            MainView()
                .environmentObject(BLEController())
                .environment(CountdownModel())
                .environment(BikerStore())
                .environmentObject(StartModel())
                .environmentObject(FinishModel())
                .environment(\.managedObjectContext, dataController.container.viewContext)
       }
    }
}
