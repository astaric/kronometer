//
//  BikerList.swift
//  Kronometer
//
//  Created by Anze Staric on 07/06/2023.
//

import SwiftUI

struct FinishList: View {
    var kronometerProvider: KronometerProvider = .shared
    @EnvironmentObject var modelData: FinishModel
    @State var error: String?

    var body: some View {
        VStack {
            List {
                ForEach(modelData.bikers) { biker in
                    FinishListItem(biker: biker, displayType: .OnTrack)
                        .swipeActions(edge: .leading) {
                            Button {
                                modelData.bikerArrived(biker)
                            } label: {
                                Text("Finish")
                            }
                        }
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
            .onAppear {
                if modelData.bikers.count == 0 {
                    Task {
                        try! await modelData.refresh()
                    }
                }
            }
            Text("\(self.error ?? "")")
        }
    }
}

struct BikerList_Previews: PreviewProvider {
    static var previews: some View {
        let model = FinishModel()
        for i in 0..<15 {
            model.bikers.append(BikerOnFinish(id: i, name: "Janez Novak", startTime: Date.now))
        }
        model.bikers.append(BikerOnFinish(id: 22, name: "Edvin Novak"))
        model.bikers.append(BikerOnFinish(id: 33, name: "Gregor Novak"))
        return FinishList()
            .environmentObject(model)
    }
}
