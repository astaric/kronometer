//
//  StartList.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct StartList: View {
    var kronometerProvider: KronometerProvider = .shared
    @Environment(BikerStore.self) var bikerStore
    @Environment(\.dismiss) private var dismiss
    @State var filter: FilterType = .ready
    @State var error: String?
    
    var readyBikers: [Biker] {
        bikerStore.bikers.filter { $0.startTime == nil }
    }
    var startedBikers: [Biker] {
        bikerStore.bikers.filter { $0.startTime != nil }
    }

    var body: some View {
        VStack {
            List {
                Picker("Tekmovalci", selection: $filter ) {
                    Text("Pripravljeni").tag(FilterType.ready)
                    Text("Na progi").tag(FilterType.started)
                }.pickerStyle(.segmented)
                
                ForEach(filter == .ready ? readyBikers : startedBikers) { biker in
                    StartListItem(biker: biker, selected: biker.id == bikerStore.nextBikerOnStart?.id)
                        .onTapGesture {
                            bikerStore.nextBikerOnStart = biker
                            dismiss()
                        }
                }
            }
            .refreshable {
                do {
                    try await bikerStore.refresh()
                } catch {
                    self.error = error.localizedDescription
                }
            }
        }
    }

enum FilterType {
    case ready, started
}
}

struct StartList_Previews: PreviewProvider {
    static var previews: some View {
        return StartList()
            .environment(BikerStore())
    }
}
