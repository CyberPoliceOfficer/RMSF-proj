import arduino_COM

arduino = arduino_COM('/dev/ttyUSB0')
arduino.send_data()
arduino.disconnect()
