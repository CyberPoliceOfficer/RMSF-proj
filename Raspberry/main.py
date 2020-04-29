import time
from datetime import datetime
from config import config
from psydatabase import psydatabase
from control import mlx90640_sampler

def main():

    start_time = time.time()

    main_params = config(filename='config.ini')
    params_database = config(filename='config.ini', section='postgresql')
    measurements_base = psydatabase(params_database)
    measurements_base.connect()
    main_params = measurements_base.fetch_params(main_params)
    CameraOne = mlx90640_sampler(32, 24, float(main_params["tempt"]))

    while True:
        start = time.time()

        [user_temp, clusters] = CameraOne.termal_sample()
        measurements_base.push_measurements (datetime.now(), main_params["serial"], user_temp, CameraOne.frame, clusters, 0)
        print(user_temp)

        end = time.time()
        print("The time: %f"%(end - start))

if __name__  == '__main__':
    main()
