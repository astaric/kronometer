//
//  FinishHome-iPad.swift
//  Kronometer
//
//  Created by Anze Staric on 8. 6. 24.
//

import SwiftUI

struct FinishHome: View {
    @Environment(BikerStore.self) var bikerStore
    @Namespace var bikerNamespace
    
    var pendingBikers: [Biker] {
        bikerStore.bikers
            .filter { $0.endTime == nil && $0.arrivedOnFinish == nil }
    }
    var arrivedBikers: [Biker] {
        bikerStore.bikers
            .filter{ $0.endTime == nil && $0.arrivedOnFinish != nil }
            .sorted(by: { cmpOptionalDate($0.arrivedOnFinish, <, $1.arrivedOnFinish) })
    }
    var finishedBikers: [Biker] {
        bikerStore.bikers
            .filter{ $0.endTime != nil }
            .sorted(by: { cmpOptionalDate($0.endTime, >, $1.endTime) })
    }
    @State var hideEventsBefore: Date?
    
    var body: some View {
        HStack {
            BikerList(pendingBikers, refreshable: true) { biker in
                BikerListItem(biker)
                    .swipeActions(edge: .leading) {
                        AnimatedButton(String(localized: "button_arrived")) {
                            bikerStore.setArrived(Date(), for: biker)
                        }
                    }
            }
            VStack {
                BikerList(arrivedBikers) { biker in
                    BikerListItem(biker)
                        .swipeActions(edge: .trailing) {
                            AnimatedButton(String(localized: "button_undo")) {
                                bikerStore.setArrived(nil, for: biker)
                            }
                        }
                }
                SensorEvents(arrived: arrivedBikers.first)
            }
            VStack{
                BikerList(finishedBikers) { biker in
                    BikerListItem(biker).swipeActions(edge: .trailing) {
                        AnimatedButton(String(localized: "button_undo")) {
                            self.hideEventsBefore = biker.arrivedOnFinish
                            bikerStore.setEndTime(nil, for: biker)
                        }
                    }
                }
                AddEvent()
            }
        }
    }
    
    func cmpOptionalDate(_ lhs: Date?, _ cmp: (Date, Date) -> Bool, _ rhs: Date?) -> Bool {
        if let lhs, let rhs {
            cmp(lhs, rhs)
        } else {
            true
        }
    }
}


struct FinishHome_iPad_Previews: PreviewProvider {
    static var previews: some View {
        return FinishHome()
            .environment(BikerStore())
            .environmentObject(SensorController())
    }
}
