//
//  SensorEvents.swift
//  Kronometer
//
//  Created by Anze Staric on 13/06/2023.
//

import SwiftUI

struct SensorEvents: View {
    @Environment(BikerStore.self) var bikerStore
    @EnvironmentObject
    var sensorController: SensorController
    var arrived: Biker?
    @State var hideEventsBefore: Date?
    
    var visibleEvents: [SensorEvent] {
        sensorController.events.filter { if let hideEventsBefore { $0.time > hideEventsBefore} else { true }}
    }
    
    var body: some View {
        List {
            ForEach(visibleEvents) { event in
                eventView(event)
            }
        }.refreshable {
            withAnimation {
                showFivePreviousEvents()
            }
        }
    }
    
    fileprivate func eventView(_ event: SensorEvent) -> some View {
        return Text(event.time.formatted(.dateTime.hour().minute().second()))
            .bold(event.manual)
            .swipeActions(edge: .leading) {
                AnimatedButton(arrived?.id.formatted() ?? "") {
                    if let arrived {
                        bikerStore.setEndTime(event.time, for: arrived)
                        hideEventsBefore = event.time
                    }
                }
            }
            .swipeActions(edge: .trailing) {
                AnimatedButton(String(localized: "button_hide")) {
                    hideEventsBefore = event.time
                }
            }
    }
    
    func showFivePreviousEvents() {
        
        guard let hideEventsBefore = self.hideEventsBefore else { return }
        let hidden = self.sensorController.events.filter { $0.time < hideEventsBefore }
        if hidden.count <= 5 {
            self.hideEventsBefore = nil
        } else {
            self.hideEventsBefore = hidden[hidden.count - 5].time
        }
        
    }
}

struct SensorEvents_Previews: PreviewProvider {
    static var previews: some View {
        let control = SensorController()
        control.addManualEvent()
        control.addManualEvent()
        return VStack {
            SensorEvents()
            AddEvent()
        }
        .environmentObject(control)
    }
}
