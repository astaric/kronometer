//
//  DebugMenu.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct DebugActions: View {
    @Environment(BikerStore.self) var bikerStore
    
    var body: some View {
            clearData
    }
    
    @State private var showConfirmation = false
    var clearData: some View {
        Button("Remove all data", role: .destructive) {
            showConfirmation = true
        }.confirmationDialog(
            "Are you sure?",
            isPresented: $showConfirmation) {
                Button("Delete all data?", role: .destructive) {
                    bikerStore.removeAllData()
                }
                
            }
    }
}


struct DebugMenu_Previews: PreviewProvider {
    static var previews: some View {
        DebugActions()
    }
}
