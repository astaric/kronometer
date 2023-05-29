//
//  BTSensors.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

struct BTSensors: View {
    @EnvironmentObject var btController: BLEController
    var body: some View {
        List {
            Text(btController.status)
        }
    }
}

struct BTSensors_Previews: PreviewProvider {
    static var previews: some View {
        let controller = BLEController()
        return BTSensors()
            .environmentObject(controller)
    }
}
