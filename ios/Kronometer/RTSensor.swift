//
//  RTSensor.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import Foundation
import CoreBluetooth

struct RTSensor {
    let peripheral: CBPeripheral
    let name: String
    let isConnected: Bool
}

extension RTSensor: Identifiable {
    var id: UUID {
        peripheral.identifier
    }
}
