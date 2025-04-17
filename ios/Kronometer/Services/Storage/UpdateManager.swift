//
//  UpdateManager.swift
//  Kronometer
//
//  Created by Anze Staric on 17. 4. 25.
//
import Foundation

actor UpdateManager {
    static let shared = UpdateManager(autosaveFilename: "updates.json")

    private let autosaveFilename: String?
    private var updates = [TimeUpdate]()
    private var sendTask: Task<Void, Never>? = nil

    private var apiManager: ApiManager
    init(apiManager: ApiManager = .shared, autosaveFilename: String? = nil) {
        self.apiManager = apiManager
        self.autosaveFilename = autosaveFilename

        if let filename = autosaveFilename,
            let loaded = Self.loadUpdates(from: filename)
        {
            self.updates = loaded
        }
    }

    // MARK: - Public Methods

    func add(_ update: TimeUpdate) async {
        updates.append(update)
        autosaveUpdates()
        scheduleSend()
    }

    /// Mark all updates with the given id as not synced and reschedule sending.
    func retry(_ update: TimeUpdate) async {
        if let idx = updates.firstIndex(where: { $0.id == update.id }) {
            updates[idx].synced = false
            scheduleSend()
        }
    }

    func getUpdates() -> [TimeUpdate] {
        return updates
    }

    func removeAllData() {
        updates = []
        autosaveUpdates()
    }

    func scheduleSend() {
        guard sendTask == nil else { return }

        sendTask = Task {
            defer { sendTask = nil }
            while true {
                let unsynced = updates.enumerated().filter { !$0.element.synced }
                guard !unsynced.isEmpty else { break }

                await self.send(updates: unsynced)
            }
        }
    }

    // MARK: - Private Helpers

    private func send(updates updatesToSend: [([TimeUpdate].Index, TimeUpdate)]) async {
        for (idx, update) in updatesToSend {
            do {
                try await apiManager.updateTimes(
                    for: update.biker, startTime: update.startTime, endTime: update.endTime)
                updates[idx].error = nil
                updates[idx].synced = true
            } catch {
                updates[idx].error = error.localizedDescription
                updates[idx].synced = true
            }
        }
        autosaveUpdates()
    }
}

extension UpdateManager {
    func autosaveUpdates() {
        guard let autosaveFilename else { return }
        do {
            let saveFile = URL.documentsDirectory.appending(path: autosaveFilename)
            let data = try JSONEncoder().encode(updates)
            try data.write(to: saveFile, options: [.atomic])
        } catch let error {
            print("Error saving data: \(error)")
        }
    }
    private static func loadUpdates(from filename: String) -> [TimeUpdate]? {
        let saveFile = URL.documentsDirectory.appending(path: filename)
        guard let data = try? Data(contentsOf: saveFile) else { return nil }

        return try? JSONDecoder().decode([TimeUpdate].self, from: data)
    }
}

struct TimeUpdate: Codable, Equatable, Hashable, Identifiable {
    var id = UUID()
    var biker: Biker
    var startTime: Date?
    var endTime: Date?
    var synced = false
    var error: String?

    enum Field: Codable {
        case startTime, endTime
    }
}
