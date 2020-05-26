import time
import sys
import signal
import csv
from datetime import datetime
from config import config
from psydatabase import psydatabase
from control import mlx90640_sampler
from control import controller
import wiringpi as piwiring


def main():
    start_time = time.time()

    # Measuring data
    temp = []
    time_v = []
    actu = []

    alarm_status = 1
    pinFan = 12

    # Carregar os ficheiros de configurações, representado as definições de fabrico
    main_params = config(filename='config.ini')
    params_database = config(filename='config.ini', section='postgresql')

    # Criar o objeto da base de dados
    measurements_base = psydatabase(params_database)
    measurements_base.connect()

    # Carregar os parametros actualizados da base de dados
    main_params = measurements_base.fetch_params(main_params)
    fetch_time = time.time()

    # Preparar o objecto da camera
    CameraOne = mlx90640_sampler(32, 24, float(main_params["tempt"]))

    # Criar objecto do controlador
    cont = controller (float(main_params["kp"]), float(main_params["ki"]), 1024)
    cont.ref = float(main_params["tempf"])

    # Preparar o pinout da FAN
    piwiring.wiringPiSetupGpio()
    piwiring.pinMode(pinFan, 2)

    # Ciclo principal do sistema
    while True:
        try:
            start = time.time()

            # Buscar actualizações dos parametros a cada fetchtime minutos
            if (start - fetch_time > float(main_params["t_updt"]) * 60):
                main_params = measurements_base.fetch_params(main_params)
                cont.ref = float(main_params["tempf"])
                fetch_time = time.time()

            # Obter uma amostra da camera termica
            [user_temp, clusters] = CameraOne.termal_sample()
            print("Temp", user_temp, "ºC")

            # Calcular valor da actuação
            actuation = cont.compute_actuation(user_temp)
            print("FAN =",int(actuation*8000/1024), "RPM")

            # Actuar a fan
            piwiring.pwmWrite(pinFan, int(actuation))

            # Enviar medições para a base de dados
            measurements_base.push_measurements(datetime.now(), main_params["serial"], user_temp, clusters, int(actuation*8000/1024))
            end = time.time()

            # Guardar localmente as medições
            temp.append(user_temp)
            time_v.append((end - start_time))
            actu.append(actuation)
            print("Delta t: %f"%(end - start))

            # Caso a temperatura seja maior que a do disparo do alarme, enviar notificação para a base de dados
            if (user_temp > float(main_params["tempw"]) and alarm_status):
                measurements_base.sound_alarm(datetime.now(), main_params["serial"])
                alarm_status = 0
                print("Warning: Overheating")

            # Resetar o alarme
            if (user_temp <= float(main_params["tempw"])):
                alarm_status = 1

        # Lidar com o Ctrl + C
        except KeyboardInterrupt:
            piwiring.pwmWrite(pinFan, 0)
            with open("data.csv", 'w', newline='') as myfile:
                wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
                wr.writerow(temp)
                wr.writerow(actu)
                wr.writerow(time_v)
            sys.exit()

        # Caso aconteça algum erro ainda não lidado no try, ignorar e continuar o ciclo
        except:
            continue


if __name__  == '__main__':
    main()
