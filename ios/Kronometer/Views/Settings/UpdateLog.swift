//
//  StartEventLog.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct UpdateLog: View {
    @Environment(BikerStore.self) var bikerStore

    @State var error: ErrorMessage?
    
    var body: some View {
        List {
            ForEach(bikerStore.updates) {update in
                HStack {
                    Text(update.biker.name)
                    Text("\(update.field)")
                    Text("\(update.value)")
                    
                    Text(update.synced ? "Y" : "N")
                }
            }
        }.refreshable {
            do {
                try await bikerStore.sendUpdates()
            } catch let error {
                self.error = ErrorMessage(message: error.localizedDescription)
            }
        }
        .popover(item: $error) { error in
            Text(error.message)
        }
    }
    
    struct ErrorMessage : Identifiable {
        var id = UUID()
        var message: String
    }
}

struct StartEventLog_Previews: PreviewProvider {
    static var previews: some View {
        UpdateLog()
    }
}
