import seeed_mlx90640
import numpy as np
from scipy.ndimage import gaussian_filter
from scipy import ndimage
import time

sizeX = 32
sizeY = 24
TempT = 32

def main():
    mlx = seeed_mlx90640.grove_mxl90640()
    mlx.refresh_rate = seeed_mlx90640.RefreshRate.REFRESH_8_HZ
    frame = [0] * sizeX * sizeY
    while True:
        start = time.time()
        try:
            mlx.getFrame(frame)
            framenp = np.array(frame)
            framenp = framenp.reshape(sizeX, sizeY)
            framegf = gaussian_filter(framenp, sigma=5)
            labeled, nr_objects = ndimage.label(framegf > TempT)
            print(nr_objects)
            print(labeled)
        except ValueError:
            continue
        end = time.time()
        print("The time: %f"%(end - start))

if __name__  == '__main__':
    main()
