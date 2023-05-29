//
//  BLEController.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import CoreBluetooth
import SwiftUI

struct LogEntry : Identifiable {
    let id: Int
    let time: Date
    let message: String

    static private var lastId = 0

    init(_ message: String) {
        LogEntry.lastId += 1
        self.id = LogEntry.lastId
        self.time = Date.now
        self.message = message
    }
}

struct SensorEvent: Identifiable {
    let id: Int
    let time: Date
    let value: Int
}

class BLEController: NSObject, ObservableObject, CBCentralManagerDelegate,CBPeripheralDelegate {
    @AppStorage("sensorId", store: .standard) private var sensorId: String = ""
    @Published var status = ""

    var myCentral: CBCentralManager!
    @Published var sensors: [RTSensor] = []
    @Published var events: [SensorEvent] = []
    @Published var logEntries: [LogEntry] = []

    let sensorServiceUUID = CBUUID(string: "19b10000-e8f2-537e-4f6c-d104768a1214")
    let sensorCharacteristicUUID = CBUUID(string: "19b10001-e8f2-537e-4f6c-d104768a1214")

    override init() {
        super.init()
        myCentral = CBCentralManager(delegate: self, queue: nil)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            self.log("Bluetooth is available")
            if let sensorId = UUID(uuidString: self.sensorId) {
                connectToSensor(sensorId)
            }
        }
        else {
            self.log("Bluetooth is not available")
        }
    }

    func connectToSensor() {
        self.log("scanning for peripherals")
        myCentral.scanForPeripherals(withServices: [sensorServiceUUID], options: nil)
    }

    func connectToSensor(_ sensorId: UUID) {
        self.log("retrieving peripheral with id \(sensorId)")
        for peripheral in myCentral.retrievePeripherals(withIdentifiers: [sensorId]) {
            peripheral.delegate = self
            sensors.append(RTSensor(peripheral: peripheral, name: peripheral.name ?? "NoName", isConnected: false))
            myCentral.connect(peripheral, options: nil)
        }
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        self.log("peripheral connected \(peripheral.services ?? [])")
        peripheral.discoverServices([sensorServiceUUID])
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        self.log("peripheral disconnected")
        central.connect(peripheral, options: nil)
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        self.log("found service")
        if let services = peripheral.services {
            peripheral.discoverCharacteristics([sensorCharacteristicUUID], for: services.first!)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        self.log("found characteristic")
        if let characteristics = service.characteristics {
            peripheral.setNotifyValue(true, for: characteristics.first!)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if error == nil {
            self.log("listening from events")
        } else {
            self.log("could not subscribe to sensor \(error!)")
        }

    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        self.log("recieved event")
        if let value = characteristic.value?.withUnsafeBytes({ $0.load(as: Int8.self) }) {
            if value == 0 {
                events.append(SensorEvent(id: events.count, time: Date.now, value: Int(value)))
            }
        }
    }

    private func log(_ message: String) {
        self.status = message
        self.logEntries.append(LogEntry(message))
    }
}
