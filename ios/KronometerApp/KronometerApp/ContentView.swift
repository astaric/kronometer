//
//  ContentView.swift
//  KronometerApp
//
//  Created by Anze Staric on 11/01/2023.
//

import SwiftUI
import CoreData

struct ContentView: View {
    enum Screen: String, Equatable, CaseIterable {
        case start = "Start"
        case finish = "Finish"
    }

    @State var currentScreen = Screen.start

    @Environment(\.managedObjectContext) private var viewContext

    @FetchRequest(
        sortDescriptors: [NSSortDescriptor(keyPath: \Item.timestamp, ascending: true)],
        animation: .default)
    private var items: FetchedResults<Item>

    var body: some View {
        NavigationStack {
            VStack {
                if currentScreen == .start {
                    StartHome()
                } else if currentScreen == .finish {
                    FinishHome()
                }
            }.toolbar {
                ToolbarItem(placement: .navigation) {
                    HStack {
                        Menu {
                            Button("Start") { currentScreen = .start }
                            Button("Finish") { currentScreen = .finish }
                        } label: {
                            Image(systemName: "line.3.horizontal")
                                .foregroundColor(.primary)
                        }
                    }
                }
            }
        }
    }
}

private let itemFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateStyle = .short
    formatter.timeStyle = .medium
    return formatter
}()

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
            .environmentObject(StartModel())
            .environmentObject(CountdownModel())
    }
}
