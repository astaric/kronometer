//
//  BikerStore.swift
//  Kronometer
//
//  Created by Anze Staric on 7. 6. 24.
//

import SwiftUI

@Observable
class BikerStore {
    private (set) var bikers: [Biker] {
        didSet {
            autosaveBikers()
        }
    }
    
    var nextBikerOnStart: Biker?
    
    init() {
        if let bikers = Self.autosavedBikers {
            self.bikers = bikers
        } else {
            self.bikers = []
        }
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
    
    func setArrived(for biker: Biker) {
        if let idx = bikers.firstIndex(where: { $0.id == biker.id }) {
            bikers[idx].arrivedOnFinish = Date()
        }
    }
    
    func setEndTime(_ endTime: Date?, for biker: Biker) {
        if let idx = bikers.firstIndex(where: { $0.id == biker.id}) {
            bikers[idx].endTime = endTime
        }
    }
    
    func refresh() async throws {
        let bikerData = try await KronometerApi.getBikers()
        bikers = bikerData.map { Biker(id: $0.number, name: "\($0.name) \($0.surname)", startTime: $0.start_time) }
        selectNextBikerToStart()
    }
}

extension BikerStore {
    static let bikersAutosaveFile = URL.documentsDirectory.appending(path: "bikers.json")
    func autosaveBikers() {
        do {
            let data = try JSONEncoder().encode(bikers)
            try data.write(to: Self.bikersAutosaveFile, options: [.atomic])
        } catch let error {
            print("Error saving data: \(error)")
        }
    }
    private static var autosavedBikers: [Biker]? {
        guard let data = try? Data(contentsOf: Self.bikersAutosaveFile) else { return nil }
        return try? JSONDecoder().decode([Biker].self, from: data)
    }
}

extension BikerStore {
    func createTestBikers() {
        bikers = [
            Biker(id: 1, name: "Anže Starič"),
            Biker(id: 2, name: "Jože Starič"),
            Biker(id: 3, name: "Julija Starič")
        ] + (4...20).map {Biker(id: $0, name: "Janez Novak \($0)")}
        self.nextBikerOnStart = bikers.first
    }
}


