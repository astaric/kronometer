//
//  AddEvent.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
//

import SwiftUI

struct AddEvent: View {
    @EnvironmentObject
    var modelData: FinishModel
    @EnvironmentObject
    var bleController: BLEController

    var body: some View {
        Button {
            modelData.addEvent(Date.now, manual: true)
        } label: {
            Text("Event")
                .frame(height: 250)
                .frame(maxWidth: .infinity)
                .font(.custom("", size: 50, relativeTo: .largeTitle))
                .foregroundColor(.primary)
                .background(Color(UIColor.secondarySystemGroupedBackground))
        }
        .onReceive(self.bleController.$events) { newValue in
            if let event = newValue.last {
                modelData.addEvent(event.time, manual: false)
            }
        }
    }
}

struct AddEvent_Previews: PreviewProvider {
    static var previews: some View {
        AddEvent()
    }
}
