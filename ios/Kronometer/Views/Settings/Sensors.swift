//
//  BTSensors.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import SwiftUI

struct Sensors: View {
    @EnvironmentObject var bleController: BLEController

    var body: some View {
        List {
            if bleController.discovering {
                Button {
                    bleController.stopDiscovery()
                } label: {
                    Text("Stop Discovery")
                }
            } else {
                Button {
                    bleController.startDiscovery()
                } label: {
                    Text("Discover sensors")
                }
            }


            ForEach(bleController.sensors) { sensor in
                Button {
                    if sensor.isConnected {
                        bleController.disconnect(sensor)
                    } else {
                        bleController.connect(sensor)
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
                .onChange(of: bleController.logEntries.count) { _ in
                    proxy.scrollTo(bleController.logEntries.count - 1)
                }
            }
        }

    }
}


struct Sensors_Previews: PreviewProvider {
    static var previews: some View {
        let controller = BLEController()
        return Sensors()
            .environmentObject(controller)
    }
}
