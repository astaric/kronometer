//
//  AddEvent.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
//

import SwiftUI

struct AddEvent: View {
    @EnvironmentObject
    var sensorController: SensorController

    var body: some View {
        Button {
            sensorController.addManualEvent()
        } label: {
            Text("Event")
                .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, maxHeight: 250)
                .font(.largeTitle)
                .foregroundColor(.primary)
        }
    }
}

struct AddEvent_Previews: PreviewProvider {
    static var previews: some View {
        AddEvent()
    }
}
