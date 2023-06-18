//
//  ClearData.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
//

import SwiftUI

struct ClearData: View {
    @EnvironmentObject
    var startModel: StartModel

    @State private var showConfirmation = false

    var body: some View {
        Button("Remove all data", role: .destructive) {
            showConfirmation = true
        }.confirmationDialog(
            "Are you sure?",
            isPresented: $showConfirmation) {
                Button("Delete all data?", role: .destructive) {
                    try? KronometerProvider.shared.removeAllData()
                    startModel.reset()
                    Task {
                        try await startModel.refresh()
                    }
                }

            }
    }
}

struct ClearData_Previews: PreviewProvider {
    static var previews: some View {
        ClearData()
            .environmentObject(StartModel())
    }
}
