//
//  StartEventLog.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct UpdateLog: View {
    private(set) var updateManager: UpdateManager? = UpdateManager.shared
    @State private(set) var updates = [TimeUpdate]()
    
    var body: some View {
        List {
            ForEach(updates) { update in
                UpdateRow(update: update, retryAction: {
                    Task {
                        await updateManager?.retry(update)
                        await refreshUpdates()
                    }
                })
            }
        }
        .task {
            while true {
                await refreshUpdates()
                try? await Task.sleep(for: .seconds(2))
            }
        }
        .refreshable {
            await updateManager?.scheduleSend()
            await refreshUpdates()
        }
        .navigationTitle(String(localized: "update_log"))
    }
    
    private func refreshUpdates() async {
        if let updateManager {
            updates = await updateManager.getUpdates()
        }
    }
    
    private struct UpdateRow: View {
        let update: TimeUpdate
        let retryAction: () -> Void
        
        var body: some View {
            VStack {
                HStack {
                    bikerNameAndTimes
                    if update.error == nil {
                        okIcon
                    } else {
                        retryButton
                    }
                }
                if let error = update.error {
                    errorMessage(error)
                }
            }
        }
        
        private var bikerNameAndTimes: some View {
            VStack {
                HStack {
                    Text(String(update.biker.id))
                    Text(update.biker.name)
                    Spacer()
                }
                if let startTime = update.startTime {
                    TimeView(caption: String(localized: "start"), time: startTime)
                }
                if let endTime = update.endTime {
                    TimeView(caption: String(localized: "finish"), time: endTime)
                }
            }
        }
        
        private var okIcon: some View {
            Image(systemName: "checkmark.circle")
                .foregroundStyle(.green)
        }
        
        private var retryButton: some View {
            Button {
                retryAction()
            } label: {
                Image(systemName: "arrow.trianglehead.2.clockwise.rotate.90.circle")
            }
        }
        
        private func errorMessage(_ error: String) -> some View {
            HStack {
                Text(error)
                    .foregroundStyle(.red)
                Spacer()
            }
        }
    }
    
    private struct TimeView: View {
        static private let formatter: DateFormatter = {
            let f = DateFormatter()
            f.dateFormat = "HH:mm:ss.SSS"
            return f
        }()
        let caption: String
        let time: Date
        
        private var formattedTime: String {
            Self.formatter.string(from: time)
        }
        
        var body: some View {
            HStack {
                Text(caption)
                Text(formattedTime)
                Spacer()
            }
        }
    }
}

struct StartEventLog_Previews: PreviewProvider {
    static var previews: some View {
        return UpdateLog(updateManager: nil, updates: [
            TimeUpdate(biker: Biker(competition_id: 0, id: 1, name: "Anže"), startTime: .now),
            TimeUpdate(biker: Biker(competition_id: 0, id: 2, name: "Jure"), endTime: .now, error: "Some error occured")
        ])
    }
}
