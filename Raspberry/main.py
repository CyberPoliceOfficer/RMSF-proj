import time
from datetime import datetime
from config import config
from psydatabase import psydatabase
from control import mlx90640_sampler

def main():

    start_time = time.time()

    params_database = config(filename='config.ini', section='postgresql')
    measurements_base = psydatabase(params_database)
    measurements_base.connect()
    CameraOne = mlx90640_sampler(32,24,28)


    while True:
        start = time.time()
        try:
            user_temp = CameraOne.termal_sample()
            measurements_base.push_measurements (datetime.now(), 'xy123cx', user_temp, CameraOne.frame, 0)
            print(user_temp)
        except ValueError:
            continue
        time.sleep(10)
        end = time.time()
        print("The time: %f"%(end - start))

if __name__  == '__main__':
    main()
