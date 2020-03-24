import psycopg2

class psydatabase:

    def __init__ (self, params):
        self._login_info = params
        self._conn = None
        self._cur = None

    def connect (self):
        try:
            self._conn = psycopg2.connect(**self._login_info)
            self._cur = self._conn.cursor()

        except (Exception, psycopg2.DatabaseError) as error:
            print(error)

    def close (self):
        if self._conn is not None:
            self._cur.close()
            self._conn.close()

    def push_measurements (self, Time_Point, Serial_Number, Temperature, Termal_Image, Fan_RPM):
        try:
            self._cur.execute('INSERT INTO Measurements VALUES (%s, %s, %s, %s, %s)', (Serial_Number, Time_Point, Termal_Image, Temperature, Fan_RPM))
            self._conn.commit()
        except (Exception, psycopg2.DatabaseError) as error:
            print(error)
