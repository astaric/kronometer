void setup() {
  Serial.begin(9600);
  delay(100);
  pinMode(2, INPUT_PULLUP);
  attachInterrupt(0, interruptHandler, FALLING);
}

long lastEventTime = 0;
boolean event = false;

void loop() {
  if (event) {
    if (millis() - lastEventTime > 10) {
        Serial.print('E');
        lastEventTime = millis();
    }

    event = false;
  }
}

void interruptHandler() {
  event = true;
}
