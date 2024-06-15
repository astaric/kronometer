//
//  EndEventLog.swift
//  Kronometer
//
//  Created by Anze Staric on 14/06/2023.
//

import SwiftUI

struct EndEventLog: View {
    @FetchRequest(sortDescriptors: [SortDescriptor(\.end_time)])
    private var events: FetchedResults<EndEvent>

    var body: some View {
        List {
            ForEach(events) {event in
                Text("biker \(event.biker_no), start time \(event.end_time?.formatted(.dateTime.hour().minute().second()) ??  ""), uploaded: \(event.uploaded ? "Yes" : "No")")
            }
        }.refreshable {
            // TODO:
        }
    }
}

struct EndEventLog_Previews: PreviewProvider {
    static var previews: some View {
        EndEventLog()
    }
}
