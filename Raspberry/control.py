import seeed_mlx90640
import numpy as np
from scipy.ndimage import gaussian_filter
from scipy import ndimage

class mlx90640_sampler:
    def  __init__ (self, input_sizeX = 32, input_sizeY = 24, input_TempT = 80, input_top_clusters = 3):
            self._mlx = seeed_mlx90640.grove_mxl90640()
            self._mlx.refresh_rate = seeed_mlx90640.RefreshRate.REFRESH_8_HZ
            self.sizeX = input_sizeX
            self.sizeY = input_sizeY
            self.TempT = input_TempT
            self.frame = [0] * self.sizeX * self.sizeY
            self.n_clusters = input_top_clusters

    def termal_sample (self):
            self._mlx.getFrame(self.frame)
            framenp = np.array(self.frame)
            frame_avg = np.mean(framenp, axis=0)
            framenp = framenp.reshape(self.sizeY, self.sizeX)
            labeled, nr_objects = ndimage.label(gaussian_filter(framenp, sigma = 5) > self.TempT)
            #print(labeled)

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

            clusters = np.zeros(0)
            if (nr_objects > 0):
                ind = np.argsort(count, axis=0)
                if (ind.size <= self.n_clusters):
                    c_ind = ind
                else:
                    c_ind = ind[1:self.n_clusters,:]

                c_of_m = ndimage.measurements.center_of_mass(framenp, labeled, c_ind+1)
                rs = np.sqrt (count[ind]/np.pi)
                rs = rs.reshape(-1,1)
                c_of_m = np.asarray(c_of_m)
                clusters = np.append (c_of_m, rs, axis=1)
                clusters = clusters.flatten()


            clusters = np.append(clusters, np.zeros((self.n_clusters-nr_objects)*3) - 1)
            return [user_temp, clusters.tolist()];
