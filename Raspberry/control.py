import seeed_mlx90640
import numpy as np
from scipy.ndimage import gaussian_filter
from scipy import ndimage

class mlx90640_sampler:
    def  __init__ (self, input_sizeX = 32, input_sizeY = 24, input_TempT = 80):
            self._mlx = seeed_mlx90640.grove_mxl90640()
            self._mlx.refresh_rate = seeed_mlx90640.RefreshRate.REFRESH_8_HZ
            self.sizeX = input_sizeX
            self.sizeY = input_sizeY
            self.TempT = input_TempT
            self.frame = [0] * self.sizeX * self.sizeY

    def termal_sample (self):
            self._mlx.getFrame(self.frame)
            framenp = np.array(self.frame)
            frame_avg = np.mean(framenp, axis=0)
            framenp = framenp.reshape(self.sizeY, self.sizeX)
            labeled, nr_objects = ndimage.label(gaussian_filter(framenp, sigma = 5) > self.TempT)
            print(labeled)

            if (nr_objects > 0):
                count = np.zeros((nr_objects,))
                sum = np.zeros((nr_objects,))
                for ix, iy in np.ndindex(labeled.shape):
                    if labeled[ix,iy] != 0:
                         sum[labeled[ix,iy]-1] += framenp[ix,iy]
                         count[labeled[ix,iy]-1] += 1
                user_temp = np.amax(sum/count, axis = 0)
            else:
                user_temp = frame_avg

            return user_temp;
