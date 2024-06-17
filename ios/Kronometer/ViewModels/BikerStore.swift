//
//  BikerStore.swift
//  Kronometer
//
//  Created by Anze Staric on 7. 6. 24.
//

import SwiftUI

@Observable
class BikerStore {
    private (set) var bikers = [Biker]() {
        didSet {
            autosaveBikers()
        }
    }
    var nextBikerOnStart: Biker?
    
    private (set) var updates = [Update]() {
        didSet {
            autosaveUpdates()
            Task {
                try? await sendUpdates()
            }
        }
    }
    
    init() {
        if let bikers = Self.autosavedBikers {
            self.bikers = bikers
        }
        if let updates = Self.autosavedUpdates {
            self.updates = updates
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
            updates.append(Update(biker: biker, field: .startTime, value: startTime))
            selectNextBikerToStart()
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
                updates.append(Update(biker: biker, field: .endTime, value: endTime))
            }
        }
    }
    
    func refresh() async throws {
        let bikerData = try await KronometerApi.getBikers()
        bikers = bikerData.map { Biker(id: $0.number, name: "\($0.name) \($0.surname)", startTime: $0.start_time, endTime: $0.end_time) }
        selectNextBikerToStart()
    }
    
    func sendUpdates() async throws {
        for idx in updates.indices.filter({ updates[$0].synced == false }) {
            let update = updates[idx]
            switch update.field {
            case .startTime:
                try await KronometerApi.setStartTime(for: update.biker.id, to: update.value)
            case .endTime:
                try await KronometerApi.setEndTime(for: update.biker.id, to: update.value)
            }
            updates[idx].synced = true
        }
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
    static let updatesAutosaveFile = URL.documentsDirectory.appending(path: "updates.json")
    func autosaveUpdates() {
        do {
            let data = try JSONEncoder().encode(updates)
            try data.write(to: Self.updatesAutosaveFile, options: [.atomic])
        } catch let error {
            print("Error saving data: \(error)")
        }
    }
    private static var autosavedUpdates: [Update]? {
        guard let data = try? Data(contentsOf: Self.updatesAutosaveFile) else { return nil }
        return try? JSONDecoder().decode([Update].self, from: data)
    }
}

extension BikerStore {
    func removeAllData() {
        bikers = []
        updates = []
        self.nextBikerOnStart = nil
    }
    
    func createTestBikers() {
        bikers = [
            Biker(id: 1, name: "Anže Starič"),
            Biker(id: 2, name: "Jože Starič"),
            Biker(id: 3, name: "Julija Starič")
        ] + (4...20).map {Biker(id: $0, name: "Janez Novak \($0)")}
        self.nextBikerOnStart = bikers.first
    }
}

struct Update: Codable, Identifiable {
    var id: Int
    var biker: Biker
    var field: Field
    var value: Date
    var synced = false
    
    enum Field: Codable {
        case startTime, endTime
    }
    
    private static var lastId = 0
    init(biker: Biker, field: Field, value: Date) {
        Self.lastId += 1
        self.id = Self.lastId
        self.biker = biker
        self.field = field
        self.value = value
    }
}

