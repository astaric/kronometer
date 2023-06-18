//
//  SensorEvents.swift
//  Kronometer
//
//  Created by Anze Staric on 13/06/2023.
//

import SwiftUI

struct SensorEvents: View {
    @EnvironmentObject
    var modelData: FinishModel

    var body: some View {
        VStack {
            List {
                ForEach(visibleEvents) { event in
                    HStack {
                        if event.manual {
                            Text(event.timestamp)
                                .bold()
                        } else {
                            Text(event.timestamp)
                        }

                    }
                        .swipeActions(edge: .leading) {
                            Button {
                                modelData.assignTime(event)
                            } label: {
                                Text("\(modelData.arrived.first?.id.formatted() ?? "")")
                            }
                        }
                        .swipeActions(edge: .trailing) {
                            Button("Hide") {
                                modelData.hideEventsUpTo(event)
                            }
                        }
                }
            }            
        }
    }


    var visibleEvents: [FinishEvent] {
        modelData.events.filter( {!$0.hidden} )
    }
}

struct SensorEvents_Previews: PreviewProvider {
    static var previews: some View {
        let model = FinishModel()
        model.events.append(FinishEvent(time: Date.now, manual: false))
        return SensorEvents()
            .environmentObject(model)
    }
}
