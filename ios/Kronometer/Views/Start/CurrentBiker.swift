//
//  CurrentBiker.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct CurrentBiker: View {
    @EnvironmentObject var modelData: StartModel

    var body: some View {
        NavigationLink(destination: StartList()) {
            if let biker = modelData.nextBiker {
                StartListItem(biker: biker)
                    .buttonStyle(.plain)
            } else {
                Text("No more bikers")
            }
        }
        .onAppear {
            if modelData.nextBikerId == nil {
                Task {
                    try! await modelData.refresh()
                    modelData.selectNextBiker()
                }
            }
        }
    }
}

struct CurrentBiker_Previews: PreviewProvider {
    static var previews: some View {
        CurrentBiker()
            .environmentObject(StartModel())
    }
}
