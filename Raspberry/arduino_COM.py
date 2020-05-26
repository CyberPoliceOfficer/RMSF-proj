import serial

class arduino_COM:
    def __init__ (self, port):
        self._serial = serial.Serial(port)

    def send_data (self):
        self._serial.write(b'hello')

    def disconnect (self):
        self._serial.close()
