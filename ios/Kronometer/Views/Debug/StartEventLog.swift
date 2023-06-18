//
//  StartEventLog.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct StartEventLog: View {
    @FetchRequest(sortDescriptors: [SortDescriptor(\.start_time)])
    private var events: FetchedResults<StartEvent>

    var body: some View {
        List {
            ForEach(events) {event in
                Text("biker \(event.biker_no), start time \(event.start_time?.formatted(.dateTime.hour().minute().second()) ??  ""), uploaded: \(event.uploaded ? "Yes" : "No")")
            }
        }.refreshable {
            do {
                try await KronometerProvider.shared.syncStartTimes()
            } catch {

            }
        }
    }
}

struct StartEventLog_Previews: PreviewProvider {
    static let kronometerProvider = KronometerProvider.shared
    static var previews: some View {
        StartEventLog()
            .environment(\.managedObjectContext, kronometerProvider.container.viewContext)
    }
}
