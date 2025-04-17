//
//  BikerStore.swift
//  Kronometer
//
//  Created by Anze Staric on 7. 6. 24.
//

import SwiftUI

@Observable
class BikerStore {
    var selectedCompetitionId: ApiService.Competition.ID?
    
    private(set) var bikers = [Biker]() {
        didSet {
            autosaveBikers()
        }
    }
    var nextBikerOnStart: Biker?
    
    let updateManager: UpdateManager
    
    init(updateManager: UpdateManager = .shared) {
        self.updateManager = updateManager
        
        if let bikers = Self.autosavedBikers {
            self.bikers = bikers
        }
    }
    
    func selectNextBikerToStart() {
        let currentBikerIdx = self.bikers.firstIndex { $0.id == nextBikerOnStart?.id }
        nextBikerOnStart = bikers.selectNext(after: currentBikerIdx) { $0.startTime == nil }
    }
    
    func setStartTime(for biker: Biker) {
        if let idx = bikers.firstIndex(where: { $0.id == biker.id }) {
            let startTime = Date()
            bikers[idx].startTime = startTime
            selectNextBikerToStart()
            Task {
                await updateManager.add(TimeUpdate(biker: biker, startTime: startTime))
            }
        }
    }
    
    func setArrived(_ value: Date?, for biker: Biker) {
        if let idx = bikers.firstIndex(where: { $0.id == biker.id }) {
            bikers[idx].arrivedOnFinish = value
        }
    }
    
    func setEndTime(_ endTime: Date?, for biker: Biker) {
        if let idx = bikers.firstIndex(where: { $0.id == biker.id}) {
            bikers[idx].endTime = endTime
            if let endTime {
                Task {
                    await updateManager.add(TimeUpdate(biker: biker, endTime: endTime))
                }
            }
        }
    }
    
    func refresh() async throws {
        let bikerData = try await ApiManager.shared.getBikers()
        bikers = bikerData.map { Biker(competition_id: $0.competition_id, id: $0.number, name: "\($0.name) \($0.surname)", startTime: $0.start_time, endTime: $0.end_time) }
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
    func removeAllData() {
        bikers = []
        self.nextBikerOnStart = nil
    }
    
    func createTestBikers() {
        bikers = [
            Biker(competition_id: 0, id: 1, name: "Anže Starič"),
            Biker(competition_id: 0, id: 2, name: "Jože Starič"),
            Biker(competition_id: 0, id: 3, name: "Julija Starič")
        ] + (4...20).map {Biker(competition_id: 0, id: $0, name: "Janez Novak \($0)")}
        self.nextBikerOnStart = bikers.first
    }
}

