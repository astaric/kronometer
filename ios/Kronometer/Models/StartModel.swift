//
//  StartModel.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import CoreData
import Foundation

struct BikerOnStart: Identifiable {
    let id: Int
    var name: String
    var startTime: Date?

    var formattedStartTime: String {
        self.startTime?.formatted(.dateTime.hour().minute().second()) ?? "waiting"
    }
}


@MainActor
class StartModel: ObservableObject {
    @Published var nextBikerId: Int? {
        didSet {
            if let idx = idToIndex[nextBikerId] {
                nextBiker = bikers[idx]
            } else {
                nextBiker = nil
            }
        }
    }
    @Published var nextBiker: BikerOnStart?
    @Published var bikers = [BikerOnStart]()
    var idToIndex: [Int?: Int] = [:]
    @Published var errorMsg = ""

    func startBiker() {
        if let idx = idToIndex[nextBikerId] {
            let bikerNo = nextBikerId!
            Task {
                do {
                    try await createStartEvent(bikerId: bikerNo, startTime: Date.now)
                } catch {
                    self.errorMsg = error.localizedDescription
                }
            }
            bikers[idx].startTime = Date.now
        }
        selectNextBiker()
    }

    func selectNextBiker() {
        let idx = idToIndex[nextBikerId] ?? 0

        for i in idx..<bikers.count {
            if bikers[i].startTime == nil {
                nextBikerId = bikers[i].id
                return
            }
        }

        for i in 0..<idx {
            if bikers[i].startTime == nil {
                nextBikerId = bikers[i].id
                return
            }
        }
        nextBikerId = nil
    }

    func refresh() async throws {
        let moc = KronometerProvider.shared.container.viewContext
        let fetchRequest = DBBiker.fetchRequest()
        fetchRequest.sortDescriptors = [NSSortDescriptor(key:"number", ascending:true)]
        let fetchedBikers: [DBBiker] = try moc.fetch(fetchRequest)
        for dbBiker in fetchedBikers {
            let biker = BikerOnStart(id: Int(dbBiker.number), name: dbBiker.name ?? "")
            if let idx = idToIndex[biker.id] {
                bikers[idx].name = biker.name
            } else {
                bikers.append(biker)
                idToIndex[biker.id] = bikers.count - 1
            }
        }
    }

    func reset() {
        bikers = []
        idToIndex = [:]
        nextBikerId = nil
    }

    private func createStartEvent(bikerId: Int, startTime: Date) async throws {
        let moc = KronometerProvider.shared.container.viewContext
        let startEvent = StartEvent(context: moc)
        startEvent.biker_no = Int32(bikerId)
        startEvent.start_time = startTime
        try moc.save()
        try await KronometerApi.shared.setStartTime(for: bikerId, to: startTime)
        startEvent.uploaded = true
        try moc.save()
    }
}
