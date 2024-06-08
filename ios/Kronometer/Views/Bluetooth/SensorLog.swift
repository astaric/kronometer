//
//  SensorLog.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct SensorLog: View {
    @EnvironmentObject var bleController: BLEController

    var body: some View {
        HStack {
            Text("Log")
            Spacer()
        }
        ScrollView {
            ScrollViewReader { proxy in
                ForEach(bleController.logEntries) { entry in
                    Text("\(entry.time.formatted(.dateTime.hour().minute().second())) \(entry.message)")
                        .frame(maxWidth: .infinity, alignment: .leading)

                }
                .onChange(of: bleController.logEntries.count) { _, _ in
                    proxy.scrollTo(bleController.logEntries.count - 1)
                }
            }
        }
    }
}

struct SensorLog_Previews: PreviewProvider {
    static var previews: some View {
        SensorLog()
    }
}
