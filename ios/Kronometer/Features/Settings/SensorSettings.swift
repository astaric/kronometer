//
//  SensorSettings.swift
//  Kronometer
//
//  Created by Anze Staric on 31/05/2023.
//

import SwiftUI

struct SensorSettings: View {
    @EnvironmentObject var sensorController: SensorController

    var body: some View {
        VStack(alignment: .leading) {
            sensorList
            Text(String(localized: "section_log"))
            sensorEventLog
        }.navigationTitle(String(localized: "bluetooth_settings"))
    }

    var sensorList: some View {
        List {
            if sensorController.discovering {
                Button {
                    sensorController.stopDiscovery()
                } label: {
                    Text(String(localized: "button_stop_discovery"))
                }
            } else {
                Button {
                    sensorController.startDiscovery()
                } label: {
                    Text(String(localized: "button_discover_sensors"))
                }
            }

            ForEach(sensorController.sensors) { sensor in
                Button {
                    if sensor.isConnected {
                        sensorController.disconnect(sensor)
                    } else {
                        sensorController.connect(sensor)
                    }
                } label: {
                    if sensor.isConnected {
                        Label(sensor.name, systemImage: "link")
                    } else {
                        Label(sensor.name, systemImage: "invalid")
                    }
                }
            }
        }
    }

    var sensorEventLog: some View {
        ScrollView {
            ForEach(sensorController.logEntries.reversed()) { entry in
                HStack {
                    Text(entry.time.formatted(.dateTime.hour().minute().second()))
                    Text(entry.message)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
    }
}

struct SensorSettings_Previews: PreviewProvider {
    static var previews: some View {
        SensorSettings()
    }
}
