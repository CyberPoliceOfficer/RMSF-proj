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

    def push_measurements (self, Time_Point, Serial_Number, Temperature, Clusters, Fan_RPM):
        try:
            if (self.conn_status):
                self._cur.execute('INSERT INTO Measurements VALUES (%s, %s, %s, %s, %s)', (Serial_Number, Time_Point, Clusters, Temperature, Fan_RPM))
                self._conn.commit()
        except (Exception, psycopg2.DatabaseError) as error:
            print("psydatabase: Error pushing")
            print(error)

    def fetch_params (self, input_params):
        try:
            if (self.conn_status):
                self._cur.execute('SELECT * FROM Thresholds WHERE serial_number = %s', (input_params['serial'],))
                rows = self._cur.fetchone()
                input_params['relay'] = rows[1]
                input_params['tempf'] = rows[2]
                input_params['tempw'] = rows[3]
                self._cur.execute('SELECT * FROM Controller WHERE serial_number = %s', (input_params['serial'],))
                rows = self._cur.fetchone()
                input_params['kp'] = rows[1]
                input_params['ki'] = rows[2]
        except (Exception, psycopg2.DatabaseError) as error:
            print("psydatabase: Error fetching")
            print(error)
        return input_params

    def sound_alarm (self, Time_Point, Serial_Number):
        try:
            if (self.conn_status):
                self._cur.execute('INSERT INTO Alarms VALUES (%s, %s)', (Serial_Number, Time_Point))
                self._conn.commit()
        except (Exception, psycopg2.DatabaseError) as error:
            print("psydatabase: Error sounding alarm")
            print(error)
