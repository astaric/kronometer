//
//  BikerStore.swift
//  Kronometer
//
//  Created by Anze Staric on 7. 6. 24.
//

import SwiftUI

@Observable
class BikerStore {
    private (set) var bikers: [Biker]
    var nextBikerOnStart: Biker?
    
    init() {
        bikers = [
            Biker(id: 1, name: "Anže Starič"),
            Biker(id: 2, name: "Jože Starič"),
            Biker(id: 3, name: "Julija Starič")
        ] + (4...20).map {Biker(id: $0, name: "Janez Novak \($0)")}
    }
    
    func selectNextBikerToStart() {
        let currentBikerIdx = self.bikers.firstIndex { $0.id == nextBikerOnStart?.id }
        nextBikerOnStart = bikers.selectNext(after: currentBikerIdx) { $0.startTime == nil }
    }
    
    func setStartTime(for biker: Biker) {
        if let idx = bikers.firstIndex(where: { $0.id == biker.id }) {
            bikers[idx].startTime = Date()
            selectNextBikerToStart()
        }
    }
    
    func refresh() async throws {
        
    }
}
