import psycopg2

class psydatabase:

    def __init__ (self, params):
        self._login_info = params
        self._conn = None
        self._cur = None
        self.conn_status = 0

    def connect (self):
        try:
            self._conn = psycopg2.connect(**self._login_info)
            self._cur = self._conn.cursor()
            self.conn_status = 1

        except (Exception, psycopg2.DatabaseError) as error:
            print(error)

    def close (self):
        if self._conn is not None:
            self._cur.close()
            self._conn.close()

    def push_measurements (self, Time_Point, Serial_Number, Temperature, Termal_Image, Fan_RPM):
        try:
            if (self.conn_status):
                self._cur.execute('INSERT INTO Measurements VALUES (%s, %s, %s, %s, %s)', (Serial_Number, Time_Point, Termal_Image, Temperature, Fan_RPM))
                self._conn.commit()
        except (Exception, psycopg2.DatabaseError) as error:
            print(error)

    def fetch_params (self, input_params):
        try:
            if (self.conn_status):
                self._cur.execute('SELECT * FROM Activations_Thresholds WHERE serial_number = %s', (input_params['serial'],))
                rows = self._cur.fetchone()
                input_params['tempf'] = rows[2]
                input_params['tempw'] = rows[3]
        except (Exception, psycopg2.DatabaseError) as error:
            print("Error fetching")
            print(error)
        return input_params
