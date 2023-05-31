//
//  StartList.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct StartList: View {
    var kronometerProvider: KronometerProvider = .shared
    @EnvironmentObject var modelData: StartModel
    @Environment(\.dismiss) private var dismiss

    @State var error = ""

    var body: some View {
        ScrollViewReader { proxy in
            VStack {
                List(selection: $modelData.nextBikerId) {
                    ForEach(modelData.bikers) { biker in
                        StartListItem(biker: biker)
                    }
                }
                .refreshable {
                    do {
                        try await kronometerProvider.fetchBikers()
                        try await modelData.refresh()
                    } catch {
                        self.error = error.localizedDescription
                    }
                }

                Text("error: \(error)")
                Button {
                    if let selectedBikerId = modelData.nextBikerId {
                        proxy.scrollTo(selectedBikerId, anchor: .top)
                    }
                } label: {
                    Text("Next: \(modelData.nextBiker?.name ?? "")")
                }.buttonStyle(.plain)
            }.onAppear {
                print("app")
            }
            .onDisappear {
                print("dis")
            }
        }
    }
}

struct StartList_Previews: PreviewProvider {
    static var previews: some View {
        let model = StartModel()
        for i in 0..<15 {
            model.bikers.append(BikerOnStart(id: i, name: "Janez Novak", startTime: Date.now))
        }
        model.bikers.append(BikerOnStart(id: 22, name: "Edvin Novak"))
        model.bikers.append(BikerOnStart(id: 33, name: "Gregor Novak"))
        model.nextBikerId = 22
        return StartList()
            .environmentObject(model)
    }
}
