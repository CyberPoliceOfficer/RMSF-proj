import serial

class arduino_COM:
    def __init__ (self, port, baudgate):
        try:
            self._serial = serial.Serial(port, baudgate, serial.EIGHTBITS, serial.PARITY_NONE, serial.STOPBITS_ONE)
            self._conn = 1
        except:
            print("arduino_COM: Error connecting to arduino")
            self._conn = 0

    # Codifica e envia para o arduino as amostras
    def push_measurements (self, temp, RPM, clusters, flag):
        if self._conn:
            buffer = bytearray([0]*12)
            b_rmp = int(temp).to_bytes(1, byteorder='big', signed=False)
            buffer[0] = b_rmp[0]

            b_rmp = int(RPM).to_bytes(1, byteorder='big', signed=False)
            buffer[1] = b_rmp[0]

            b_rmp = int(flag).to_bytes(1, byteorder='big', signed=False)
            buffer[2] = b_rmp[0]

            n = 3
            for i in clusters:
                b_rmp = int(i).to_bytes(1, byteorder='big', signed=False)
                buffer[n] = b_rmp[0]
                n = n + 1

            self._serial.write(buffer)

    # Lê do arduino as amostras
    def fetch_params (self):
        try:
            if self._conn:
                readbytes = self._serial.in_waiting
                if (readbytes >= 2):
                    buffer = self._serial.read(readbytes)
                    print("arduino_COM: Message Received")
                    return [buffer[0], buffer[1]]
            return [0,0]
        except (Exception) as error:
            print("arduino_COM: Error Fetching")
            print(error)
            return [0,0]

    def disconnect (self):
        self._serial.close()
