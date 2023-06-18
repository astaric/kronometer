//
//  BLEController.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import CoreBluetooth
import SwiftUI

struct Sensor: Identifiable {
    let peripheral: CBPeripheral
    let name: String
    var isConnected: Bool

    var id: UUID {
        peripheral.identifier
    }
}

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
    @AppStorage("sensorId")
    private var sensorId: String = ""
    @AppStorage("sensorName")
    private var sensorName: String = ""

    @Published var status = ""

    var myCentral: CBCentralManager!
    @Published var sensors: [Sensor] = []
    @Published var events: [SensorEvent] = []
    @Published var logEntries: [LogEntry] = []

    @Published var discovering = false
    private var discoveryTimer: Timer?

    let sensorServiceUUID = CBUUID(string: "19b10000-e8f2-537e-4f6c-d104768a1214")
    let sensorCharacteristicUUID = CBUUID(string: "19b10001-e8f2-537e-4f6c-d104768a1214")

    override init() {
        super.init()
        myCentral = CBCentralManager(delegate: self, queue: nil)
    }

    func connect(_ sensor: Sensor?) {
        if let sensor = sensor {
            self.sensorId = sensor.id.uuidString
            self.sensorName = sensor.name
            self.connectToSensor()
        } else {
            self.sensorId = ""
            self.sensorName = ""
        }
    }

    func disconnect(_ sensor: Sensor) {
        myCentral.cancelPeripheralConnection(sensor.peripheral)
    }

    func connectToSensor() {
        guard let sensorId = UUID(uuidString: sensorId) else { return }

        for peripheral in myCentral.retrievePeripherals(withIdentifiers: [sensorId]) {
            self.log("connecting to \(sensorName)")
            peripheral.delegate = self
            addSensor(peripheral)
            myCentral.connect(peripheral, options: nil)
            return
        }
        self.log("could not connect to sensor")
        connect(nil)
    }
}

extension BLEController {

    func startDiscovery() {
        if let discoveryTimer = discoveryTimer {
            discoveryTimer.invalidate()
        }
        self.log("scanning for sensors")
        discovering = true
        myCentral.scanForPeripherals(withServices: [sensorServiceUUID], options: nil)
        discoveryTimer = Timer.scheduledTimer(timeInterval: 10.0, target: self, selector: #selector(stopDiscovery), userInfo: nil, repeats: false)
    }

    @objc func stopDiscovery() {
        myCentral.stopScan()
        discovering = false
    }

    func addSensor(_ peripheral: CBPeripheral) {
        if sensors.filter({ $0.peripheral == peripheral}).isEmpty {
            sensors.append(Sensor(peripheral: peripheral, name: peripheral.name ?? "NoName", isConnected: false))
        }
    }
}

extension BLEController {
    // MARK: CentralManager

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            self.log("Bluetooth is available")
            if self.sensorId != "" {
                connectToSensor()
            }
        }
        else {
            self.log("Bluetooth is not available")
        }
    }

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        addSensor(peripheral)
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        self.log("sensor connected")
        peripheral.discoverServices([sensorServiceUUID])
        for index in 0..<sensors.count {
            if sensors[index].peripheral == peripheral {
                sensors[index].isConnected = true
            }
        }
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        self.log("sensor disconnected")
        central.connect(peripheral, options: nil)
        for index in 0..<sensors.count {
            if sensors[index].peripheral == peripheral {
                sensors[index].isConnected = true
            }
        }
    }
}

extension BLEController {

    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let services = peripheral.services {
            peripheral.discoverCharacteristics([sensorCharacteristicUUID], for: services.first!)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let characteristics = service.characteristics {
            peripheral.setNotifyValue(true, for: characteristics.first!)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if error == nil {
            self.log("listening for events")
        } else {
            self.log("could not subscribe to sensor \(error!)")
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if let value = characteristic.value?.withUnsafeBytes({ $0.load(as: Int8.self) }) {
            if value == 0 {
                let now = Date.now
                if let lastEvent = events.last {
                    if Calendar.current.dateComponents([.nanosecond], from: lastEvent.time, to: now).nanosecond ?? 0 < 100_000_000 {
                        return
                    }
                }
                events.append(SensorEvent(id: events.count, time: Date.now, value: Int(value)))
            }
        }
    }

    // MARK: Logging
    private func log(_ message: String) {
        self.status = message
        self.logEntries.append(LogEntry(message))
    }
}


