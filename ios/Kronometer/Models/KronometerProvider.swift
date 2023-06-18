//
//  DataController.swift
//  Kronometer
//
//  Created by Anze Staric on 30/05/2023.
//

import CoreData
import OSLog

class KronometerProvider: ObservableObject {
    let logger = Logger(subsystem: "net.staric.kronometer", category: "persistence")

    static let shared = KronometerProvider()

    let container = NSPersistentContainer(name: "Kronometer")

    init() {
        container.loadPersistentStores { description, error in
            if let error = error {
                print("Core Data failed to load: \(error.localizedDescription)")
            }
        }
    }

    func fetchBikers() async throws {
        let bikerPropertyList = try await KronometerApi.shared.getBikers()
        try await importBikers(from: bikerPropertyList)
    }

    func importBikers(from propertiesList: [BikerData]) async throws {
        guard !propertiesList.isEmpty else { return }

        let taskContext = container.newBackgroundContext()
        taskContext.mergePolicy = NSMergeByPropertyObjectTrumpMergePolicy

        taskContext.name = "importContext"
        taskContext.transactionAuthor = "importBikers"

        try await taskContext.perform {
            let batchInsertRequest = self.newBatchInsertRequest(with: propertiesList)
            if let fetchResult = try? taskContext.execute(batchInsertRequest),
               let batchInsertResult = fetchResult as? NSBatchInsertResult,
               let success = batchInsertResult.result as? Bool, success {
                return
            }
            self.logger.debug("Failed to execute batch insert request.")
            throw BikerError.batchInsertError
        }
    }

    func newBatchInsertRequest(with propertyList: [BikerData]) -> NSBatchInsertRequest {
        var index = 0
        let total = propertyList.count

        let batchInsertRequest = NSBatchInsertRequest(entity: DBBiker.entity()) { dictionary in
            guard index < total else { return true }
            dictionary.addEntries(from: propertyList[index].dictionaryValue)
            index += 1
            return false
        }
        return batchInsertRequest
    }

    func removeAllData() throws {
        for entityName in ["DBBiker", "StartEvent"] {
            let fetchRequest: NSFetchRequest<NSFetchRequestResult> = NSFetchRequest(entityName: entityName)
            let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
            deleteRequest.resultType = .resultTypeObjectIDs
            let context  = container.viewContext
            let batchDelete = try context.execute(deleteRequest) as? NSBatchDeleteResult

            guard let deleteResult = batchDelete?.result as? [NSManagedObjectID]
            else { return }

            let deletedObjects: [AnyHashable: Any] = [
                NSDeletedObjectsKey: deleteResult
            ]
            NSManagedObjectContext.mergeChanges(
                fromRemoteContextSave: deletedObjects,
                into: [context]
            )
        }
    }

    func syncStartTimes() async throws {
        let moc = container.newBackgroundContext()
        let fetchRequest = StartEvent.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "uploaded == NO")
        let pendingStartEvents: [StartEvent] = try moc.fetch(fetchRequest)
        for startEvent in pendingStartEvents {
            if let start_time = startEvent.start_time {
                try await KronometerApi.shared.setStartTime(for: Int(startEvent.biker_no), to: start_time)
            }
            startEvent.uploaded = true
            try moc.save()
        }
    }

    func syncEndTimes() async throws {
        let moc = container.newBackgroundContext()
        let fetchRequest = EndEvent.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "uploaded == NO")
        let pendingEndEvents: [EndEvent] = try moc.fetch(fetchRequest)
        for endEvent in pendingEndEvents {
            if let end_time = endEvent.end_time {
                try await KronometerApi.shared.setEndTime(for: Int(endEvent.biker_no), to: end_time)
            }
            endEvent.uploaded = true
            try moc.save()
        }
    }
}
