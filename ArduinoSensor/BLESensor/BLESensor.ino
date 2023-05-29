#include <ArduinoBLE.h>

const char* deviceServiceUuid = "19b10000-e8f2-537e-4f6c-d104768a1214";
const char* deviceServiceCharacteristicUuid = "19b10001-e8f2-537e-4f6c-d104768a1214";
BLEService sensorService(deviceServiceUuid);
BLEByteCharacteristic sensorCharacteristic(deviceServiceCharacteristicUuid, BLENotify);
BLEDescriptor sensorDescriptor("2901", "Sensor");

int BUTTON_PIN = 2;

void setup() {
  Serial.begin(9600);
  while (!Serial);

  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(BUTTON_PIN, INPUT_PULLUP);

  if (!BLE.begin()) {
    Serial.println("- Starting Bluetooth® Low Energy module failed!");
  } else {
    Serial.println("- Started Bluetooth® Low Energy module");
  }

  BLE.setDeviceName("Nano 33 BLE (1)");
  BLE.setLocalName("Nano 33 BLE (1)");
  BLE.setAdvertisedService(sensorService);  
  sensorCharacteristic.addDescriptor(sensorDescriptor);
  sensorService.addCharacteristic(sensorCharacteristic);
  
  BLE.addService(sensorService);
  sensorCharacteristic.writeValue(0);
  BLE.advertise();

  Serial.println("Nano 33 BLE (1)");
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
      int buttonState = digitalRead(BUTTON_PIN);
      if (buttonState != previous) {
        sensorCharacteristic.writeValue(buttonState);
        Serial.print("Button state changed to: ");
        Serial.println(buttonState);
        previous = buttonState;
      }
    }
    digitalWrite(LED_BUILTIN, LOW);
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
}
