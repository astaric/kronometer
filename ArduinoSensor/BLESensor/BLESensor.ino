#include <ArduinoBLE.h>

const int DEBUG = 1;
const char* deviceServiceUuid = "19b10000-e8f2-537e-4f6c-d104768a1214";
const char* deviceServiceCharacteristicUuid = "19b10001-e8f2-537e-4f6c-d104768a1214";
BLEService sensorService(deviceServiceUuid);
BLEByteCharacteristic sensorCharacteristic(deviceServiceCharacteristicUuid, BLENotify);
BLEDescriptor sensorDescriptor("2901", "Sensor");

const char* sensorNames[] = {
  "Sensor",
  "Airwaves",
  "Box",
};

const char* getSensorName() {
  int id1 = NRF_FICR->DEVICEID[0];
  switch (id1) {
    case 3130652232:
      return sensorNames[1];
    case 1374214276:
      return sensorNames[2];
    default:
      return sensorNames[0];
  }
}

int event = 0;
void sensorActivated() {
  event = 1;
}

int BUTTON_PIN = 3;

void setup() {
  Serial.begin(9600);
  if (DEBUG) {
    delay(100);
  }

  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(3), sensorActivated, FALLING);

  if (!BLE.begin()) {
    Serial.println("- Starting Bluetooth® Low Energy module failed!");
  } else {
    Serial.println("- Started Bluetooth® Low Energy module");
  }

  const char* sensorName = getSensorName();
  BLE.setDeviceName(sensorName);
  BLE.setLocalName(sensorName);
  BLE.setAdvertisedService(sensorService);  
  sensorCharacteristic.addDescriptor(sensorDescriptor);
  sensorService.addCharacteristic(sensorCharacteristic);
  
  BLE.addService(sensorService);
  sensorCharacteristic.writeValue(0);
  BLE.advertise();

  Serial.println(sensorName);
  Serial.println("");
  Serial.println("Waiting for connections");
}

void loop() {
  BLEDevice central = BLE.central();

  if (central) {
    Serial.print("Connected to central: ");
    Serial.println(central.address());    
  
    digitalWrite(LED_BUILTIN, HIGH);
    long previous = -1;
    while (central.connected()) {
      if (event) {
        sensorCharacteristic.writeValue(1);
        Serial.println("Sensor event");
        event = 0;
        sensorCharacteristic.writeValue(0);
      }
    }
    digitalWrite(LED_BUILTIN, LOW);
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
}
