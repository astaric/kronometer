//
//  BLEController.swift
//  Kronometer
//
//  Created by Anze Staric on 29/05/2023.
//

import CoreBluetooth
import SwiftUI

struct SensorInfo: Codable, Equatable {
    var id: UUID
    var name: String
}

struct Sensor: Identifiable {
    let peripheral: CBPeripheral
    var name: String
    var isConnected: Bool

    var id: UUID {
        peripheral.identifier
    }
}

struct LogEntry: Identifiable {
    let id = UUID()
    let time: Date
    let message: String

    static private var lastId = 0

    init(_ message: String) {
        self.time = Date.now
        self.message = message
    }
}

struct SensorEvent: Hashable, Identifiable {
    let id = UUID()
    let time: Date
    let value: Int
    let manual: Bool
}

class SensorController: NSObject, ObservableObject {
    @Published private(set) var sensorInfo: SensorInfo? {
        didSet {
            saveSensorInfo()
        }
    }
    private let sensorInfoKey = "sensorInfo"

    @Published private(set) var sensorConnected: Bool = false

    @Published var status = ""

    var myCentral: CBCentralManager!
    @Published var sensors: [Sensor] = []
    @Published var events: [SensorEvent] = []
    @Published var lastSensorEvent: SensorEvent?
    @Published var logEntries: [LogEntry] = []

    @Published var discovering = false
    private var discoveryTimer: Timer?

    let sensorServiceUUID = CBUUID(string: "19b10000-e8f2-537e-4f6c-d104768a1214")
    let sensorCharacteristicUUID = CBUUID(string: "19b10001-e8f2-537e-4f6c-d104768a1214")

    override init() {
        super.init()
        loadSensorInfo()
        myCentral = CBCentralManager(delegate: self, queue: nil)
    }

    func addManualEvent() {
        addEvent(manual: true)
    }

    private func addEvent(value: Int = 1, manual: Bool = false) {
        events.append(SensorEvent(time: Date(), value: 1, manual: manual))
        if !manual {
            lastSensorEvent = events.last
        }
    }

    func connect(_ sensor: Sensor?) {
        if let sensor {
            sensorInfo = SensorInfo(id: sensor.id, name: sensor.name)
            connectToSensor()
        } else {
            sensorConnected = false
            sensorInfo = nil
        }
    }

    func disconnect(_ sensor: Sensor) {
        myCentral.cancelPeripheralConnection(sensor.peripheral)
        sensorInfo = nil
    }

    private func connectToSensor() {
        guard let sensorInfo else { return }

        for peripheral in myCentral.retrievePeripherals(withIdentifiers: [sensorInfo.id]) {
            log(
                String(
                    localized: "bluetooth_connecting_to_sensor",
                    defaultValue: "Connecting to \(sensorInfo.name)"))
            peripheral.delegate = self
            addSensor(peripheral)
            myCentral.connect(peripheral, options: nil)
            return
        }
        log(String(localized: "bluetooth_could_not_connect_to_sensor"))
        connect(nil)
    }
}

extension SensorController {
    func startDiscovery() {
        if let discoveryTimer = discoveryTimer {
            discoveryTimer.invalidate()
        }
        self.log(String(localized: "bluetooth_scanning_for_sensors"))
        discovering = true
        myCentral.scanForPeripherals(withServices: [sensorServiceUUID], options: nil)
        discoveryTimer = Timer.scheduledTimer(
            timeInterval: 10.0, target: self, selector: #selector(stopDiscovery), userInfo: nil,
            repeats: false)
    }

    @objc func stopDiscovery() {
        myCentral.stopScan()
        discovering = false
    }

    func addSensor(_ peripheral: CBPeripheral) {
        if let idx = sensors.firstIndex(where: { $0.peripheral == peripheral }) {
            sensors[idx].name =
                peripheral.name ?? String(localized: "bluetooth_default_peripheral_name")
        } else {
            sensors.append(
                Sensor(
                    peripheral: peripheral,
                    name: peripheral.name ?? String(localized: "bluetooth_default_peripheral_name"),
                    isConnected: false))
        }
    }
}

// MARK: CBCentralManagerDelegate
extension SensorController: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            log(String(localized: "bluetooth_available"))
            if sensorInfo != nil {
                connectToSensor()
            }
        } else {
            log(String(localized: "bluetooth_not_available"))
        }
    }

    func centralManager(
        _ central: CBCentralManager,
        didDiscover peripheral: CBPeripheral,
        advertisementData: [String: Any],
        rssi RSSI: NSNumber
    ) {
        addSensor(peripheral)
    }

    func centralManager(
        _ central: CBCentralManager,
        didConnect peripheral: CBPeripheral
    ) {
        log(String(localized: "bluetooth_sensor_connected"))
        sensorConnected = true
        peripheral.discoverServices([sensorServiceUUID])
        if let idx = sensors.firstIndex(where: { $0.peripheral == peripheral }) {
            sensors[idx].isConnected = true
        }
    }

    func centralManager(
        _ central: CBCentralManager,
        didDisconnectPeripheral peripheral: CBPeripheral,
        error: Error?
    ) {
        sensorConnected = false
        if let idx = sensors.firstIndex(where: { $0.peripheral == peripheral }) {
            sensors[idx].isConnected = false
        }
        log(String(localized: "bluetooth_sensor_disconnected"))
        if sensorInfo != nil {
            central.connect(peripheral, options: nil)
        }
    }
}

// MARK: CBPeripheralDelegate
extension SensorController: CBPeripheralDelegate {
    func peripheral(
        _ peripheral: CBPeripheral,
        didDiscoverServices error: Error?
    ) {
        guard let service = peripheral.services?.first else { return }
        peripheral.discoverCharacteristics([sensorCharacteristicUUID], for: service)
    }

    func peripheral(
        _ peripheral: CBPeripheral,
        didDiscoverCharacteristicsFor service: CBService,
        error: Error?
    ) {
        guard let characteristic = service.characteristics?.first else { return }
        peripheral.setNotifyValue(true, for: characteristic)
    }

    func peripheral(
        _ peripheral: CBPeripheral,
        didUpdateNotificationStateFor characteristic: CBCharacteristic,
        error: Error?
    ) {
        if let error {
            self.log(
                String(
                    localized: "bluetooth_could_not_subscribe",
                    defaultValue: "Could not subscribe to sensor \(error.localizedDescription)"))
        } else {
            self.log(String(localized: "bluetooth_listening_for_events"))
        }
    }

    func peripheral(
        _ peripheral: CBPeripheral,
        didUpdateValueFor characteristic: CBCharacteristic,
        error: Error?
    ) {
        if let byte = characteristic.value?.first {
            let value = Int8(bitPattern: byte)
            if value == 0 {
                let now = Date.now
                if let lastEvent = events.last {
                    if Calendar.current.dateComponents([.nanosecond], from: lastEvent.time, to: now)
                        .nanosecond ?? 0 < 100_000_000
                    {
                        return
                    }
                }
                addEvent(value: Int(value))
            }
        }
    }
}

extension SensorController {
    private func log(_ message: String) {
        self.status = message
        self.logEntries.append(LogEntry(message))
    }
}

extension SensorController {
    private func loadSensorInfo() {
        if let loaded: SensorInfo = UserDefaults.standard.codable(forKey: self.sensorInfoKey) {
            sensorInfo = loaded
        }
    }

    private func saveSensorInfo() {
        if let sensorInfo {
            UserDefaults.standard.setCodable(sensorInfo, forKey: sensorInfoKey)
        } else {
            UserDefaults.standard.removeObject(forKey: sensorInfoKey)
        }
    }
}
