//
//  DebugMenu.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct DebugMenu: View {
    var body: some View {
        List {
            NavigationLink(destination: StartEventLog()) {
                Text("Start events")
            }
            NavigationLink(destination: EndEventLog()) {
                Text("End events")
            }
            ClearData()

        }
    }
}

struct DebugMenu_Previews: PreviewProvider {
    static var previews: some View {
        DebugMenu()
    }
}
