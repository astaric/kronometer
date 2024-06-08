//
//  FinishModel.swift
//  Kronometer
//
//  Created by Anze Staric on 13/06/2023.
//

import Foundation

struct BikerOnFinish: Identifiable {
    let id: Int
    var name: String
    var startTime: Date?

    var arrived = false
    var endTime: Date?

    var formattedStartTime: String {
        return DateFormatter.hms.string(for: startTime) ?? "waiting"
    }

    var formattedDuration: String {
        let fmt = DateComponentsFormatter()
        return fmt.string(from: startTime, to: endTime) ?? "on track"
    }
}

struct FinishEvent: Identifiable {
    let id: Int
    private static var lastId: Int = 0
    let time: Date
    let biker: BikerOnFinish?
    var hidden: Bool
    var manual: Bool

    init(time: Date, manual: Bool) {
        FinishEvent.lastId += 1
        self.id = FinishEvent.lastId
        self.time = time
        self.biker = nil
        self.hidden = false
        self.manual = manual
    }

    var timestamp: String {
        return DateFormatter.hmsms.string(for: self.time) ?? "???"
    }
}


@MainActor
class FinishModel: ObservableObject {
    @Published var bikers = [BikerOnFinish]()

    @Published var events = [FinishEvent]()

    @Published var arrived = [BikerOnFinish]()
    @Published var completed = [BikerOnFinish]()
}

// Biker
extension FinishModel {
    func bikerArrived(_ biker: BikerOnFinish) {
        for i in 0..<bikers.count {
            if bikers[i].id == biker.id {
                var biker = bikers.remove(at: i)
                biker.arrived = true
                arrived.append(biker)
                break
            }
        }
    }

    func undoAssignTime(_ biker: BikerOnFinish) {
        for i in 0..<completed.count {
            if completed[i].id == biker.id {
                let biker = completed.remove(at: i)
                arrived.append(biker)
                break
            }
        }
    }

    func assignTime(_ event: FinishEvent) {
        if var biker = arrived.first {
            Task {
                try await createEndEvent(bikerId: biker.id, endTime: event.time)

                arrived.removeFirst()
                biker.endTime = event.time
                completed.append(biker)
            }
        }
    }

    private func createEndEvent(bikerId: Int, endTime: Date) async throws {
        let moc = KronometerProvider.shared.container.viewContext
        let endEvent = EndEvent(context: moc)
        endEvent.biker_no = Int32(bikerId)
        endEvent.end_time = endTime
        try moc.save()
        try await KronometerApi.shared.setEndTime(for: bikerId, to: endTime)
        endEvent.uploaded = true
        try moc.save()
    }
}

// Sensor events
extension FinishModel {
    func addEvent(_ time: Date, manual: Bool) {
        events.append(FinishEvent(time: time, manual: manual))
    }

    func hideEventsUpTo(_ event: FinishEvent) {
        for i in 0..<events.count {
            if events[i].time <= event.time {
                events[i].hidden = true
            }
        }
    }
}


// Refresh from core-data source
extension FinishModel {
    func refresh() async throws {
        let moc = KronometerProvider.shared.container.viewContext
        let fetchRequest = DBBiker.fetchRequest()
        fetchRequest.sortDescriptors = [NSSortDescriptor(key:"number", ascending:true)]
        let fetchedBikers: [DBBiker] = try moc.fetch(fetchRequest)
        var bikerIdToIndex = [Int:Int]()
        for (index, biker) in bikers.enumerated() {
            bikerIdToIndex[biker.id] = index
        }
        var arrivedIdToIndex = [Int:Int]()
        for (index, biker) in arrived.enumerated() {
            arrivedIdToIndex[biker.id] = index
        }
        var completedIdToIndex = [Int:Int]()
        for (index, biker) in completed.enumerated() {
            completedIdToIndex[biker.id] = index
        }

        for dbBiker in fetchedBikers {
            let biker = BikerOnFinish(id: Int(dbBiker.number), name: dbBiker.name ?? "", startTime: dbBiker.start_time)
            if let idx = bikerIdToIndex[biker.id] {
                bikers[idx].name = biker.name
                bikers[idx].startTime = biker.startTime
            } else if let idx = arrivedIdToIndex[biker.id] {
                arrived[idx].name = biker.name
                arrived[idx].startTime = biker.startTime
            } else if let idx = completedIdToIndex[biker.id] {
                completed[idx].name = biker.name
                completed[idx].startTime = biker.startTime
            } else {
                bikers.append(biker)
            }
        }
    }
}
