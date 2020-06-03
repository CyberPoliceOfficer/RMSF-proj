import time
import sys
import signal
import csv
import math
from datetime import datetime
from config import config
from config import set_setting
from psydatabase import psydatabase
from control import mlx90640_sampler
from control import controller
import wiringpi as piwiring
from arduino_COM import arduino_COM


def main():
    start_time = time.time()

    # Vectores de amostragem
    temp = []
    time_v = []
    actu = []

    alarm_status = 1
    alarm_flag = 0
    pinFan = 12
    pinRelay = 13

    # Carregar os ficheiros de configurações, representado as definições de fabrico
    main_params = config(filename='config.ini')
    params_database = config(filename='config.ini', section='postgresql')

    # Inicializar porta-serie
    LoRaWAN_interface = arduino_COM(main_params["port"], 115200);
    updt_time = time.time()

    # Criar o objeto da base de dados
    measurements_base = psydatabase(params_database)

    # Connectar à base de dados por WiFi se esta flag estiver enabled
    if (int(main_params["postgresql_enabled"])):
        measurements_base.connect()

    # Carregar os parametros actualizados da base de dados
    main_params = measurements_base.fetch_params(main_params)
    fetch_time = time.time()

    # Preparar o objecto da camera
    CameraOne = mlx90640_sampler(32, 24, float(main_params["tempt"]))

    # Criar objecto do controlador
    cont = controller (float(main_params["kp"]), float(main_params["ki"]), 1023)
    cont.ref = float(main_params["tempf"])

    # Preparar o pinout da FAN
    piwiring.wiringPiSetupGpio()
    piwiring.pinMode(pinFan, 2)

    # Preparar relay simulado
    piwiring.pinMode(pinRelay, 2)

    # Ciclo principal do sistema
    while True:
        try:
            start = time.time()
            # Obter uma amostra da camera termica
            [user_temp, clusters] = CameraOne.termal_sample()


            # Caso a temperatura seja maior que a do disparo do alarme, enviar notificação para a base de dados
            if (user_temp > float(main_params["tempw"]) and alarm_status):
                measurements_base.sound_alarm(datetime.now(), main_params["serial"])
                alarm_status = 0
                alarm_flag = 1
                print("Warning: Overheating")

            # Resetar o alarme
            if (user_temp <= float(main_params["tempw"])):
                alarm_status = 1

            # Calcular valor de actuação
            actuation = cont.compute_actuation(user_temp)

            # Actuar a fan
            piwiring.pwmWrite(pinFan, int(actuation))

            # Enviar medições para a base de dados via SQL
            measurements_base.push_measurements(datetime.now(), main_params["serial"], user_temp, clusters, int(actuation*8000/1024))

            # Buscar actualizações via SQL dos parametros a cada fetchtime minutos
            if (start - fetch_time > float(main_params["t_fetch"]) * 60):
                main_params = measurements_base.fetch_params(main_params)
                cont.ref = float(main_params["tempf"])
                fetch_time = time.time()


            # Enviar medições para o arduino
            if (start - updt_time > float(main_params["t_updt"]) * 60):
                print("Data sent:")
                print("Temp", user_temp, "ºC")
                print("FAN =",int(actuation*8000/1023), "RPM")
                updt_time = time.time()
                LoRaWAN_interface.push_measurements((round(user_temp)), (math.floor(actuation/4)), clusters, alarm_flag)
                if (alarm_flag):
                    alarm_flag = 0
                if (main_params["relay"]):
                    piwiring.pwmWrite(pinRelay, 1023)



            # Buscar actualizações ao arduino
            [code, payload] = LoRaWAN_interface.fetch_params()

            # Responder devidamente ás alterações pretendidas
            if(code == ord('p')): #activar/desactivar relay
                main_params["relay"] = payload
                set_setting ('config.ini', 'main', 'relay', str(payload))

            if(code == ord('f')): #temperatura alvo da fan
                main_params["tempf"] = payload
                cont.ref = float(main_params["tempf"])
                set_setting ('config.ini', 'main', 'tempf', str(payload))

            if(code == ord('r')): #temperatura do alarme
                main_params["tempw"] = payload
                cont.ref = float(main_params["tempw"])
                set_setting ('config.ini', 'main', 'tempw', str(payload))

            end = time.time()
            # Guardar localmente as medições
            temp.append(user_temp)
            time_v.append((end - start_time))
            actu.append(actuation)


        # Lidar com o Ctrl + C
        except KeyboardInterrupt:
            piwiring.pwmWrite(pinFan, 0)
            LoRaWAN_interface.disconnect()
            with open("data.csv", 'w', newline='') as myfile:
                wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
                wr.writerow(temp)
                wr.writerow(actu)
                wr.writerow(time_v)
            sys.exit()

        # Caso aconteça algum erro ainda não lidado no try, ignorar e continuar o ciclo
        except (Exception) as error:
            print("Unknown error, ignoring.")
            print(error)
            continue


if __name__  == '__main__':
    main()
