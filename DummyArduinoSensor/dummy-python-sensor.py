from datetime import datetime
import serial
import time

s = serial.Serial('/dev/tty.Bluetooth-Incoming-Port', 115200)

while True:
      s.write('E')
      s.flush()
      print(datetime.now())
      time.sleep(10)
