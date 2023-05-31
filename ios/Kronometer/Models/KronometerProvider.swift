//
//  DataController.swift
//  Kronometer
//
//  Created by Anze Staric on 30/05/2023.
//

import CoreData
import OSLog

class KronometerProvider: ObservableObject {
    let bikerListUrl = URL(string:"https://kronometer.staric.net/biker/list")!

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
        let sesion = URLSession.shared
        guard let (data, response) = try? await sesion.data(from: bikerListUrl),
              let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200
        else {
            logger.debug("Failed to receive valid response and/or data")
            throw BikerError.fetchError
        }

        do {
            let bikerPropertyList = try parseBikersJson(jsonData: data)
            try await importBikers(from: bikerPropertyList)
        } catch {
            throw BikerError.wrongDataFormat(error: error)
        }
    }

    func importBikers(from propertiesList: [BikerProperties]) async throws {
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

    func newBatchInsertRequest(with propertyList: [BikerProperties]) -> NSBatchInsertRequest {
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
}


