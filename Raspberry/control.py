import seeed_mlx90640
import numpy as np
from scipy.ndimage import gaussian_filter
from scipy import ndimage

# Classe do amostrador
class mlx90640_sampler:
    def  __init__ (self, input_sizeX = 32, input_sizeY = 24, input_TempT = 80, input_top_clusters = 3):
            self._mlx = seeed_mlx90640.grove_mxl90640()
            self._mlx.refresh_rate = seeed_mlx90640.RefreshRate.REFRESH_8_HZ
            self.sizeX = input_sizeX
            self.sizeY = input_sizeY
            self.TempT = input_TempT
            self.frame = [0] * self.sizeX * self.sizeY
            self.n_clusters = input_top_clusters

    # rotina que amostra e processa a imagem termica
    def termal_sample (self):
            self._mlx.getFrame(self.frame)
            framenp = np.array(self.frame)
            frame_avg = np.mean(framenp, axis=0)
            framenp = framenp.reshape(self.sizeY, self.sizeX)
            labeled, nr_objects = ndimage.label(gaussian_filter(framenp, sigma = 5) > self.TempT)

            # calculo do user temp
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

            # calculo das clusters
            clusters = np.zeros(0)
            if (nr_objects > 0):
                ind = np.argsort(count, axis=0)
                ind = np.flip(ind)
                if (ind.size <= self.n_clusters):
                    c_ind = ind
                else:
                    c_ind = ind[0:self.n_clusters]

                c_ind = c_ind+1

                c_of_m = ndimage.measurements.center_of_mass(framenp, labeled, c_ind)
                rs = np.sqrt (count[c_ind-1]/np.pi)
                rs = rs.reshape(-1,1)
                c_of_m = np.asarray(c_of_m)
                clusters = np.append (c_of_m, rs, axis=1)
                clusters = clusters.flatten()


            if (nr_objects < self.n_clusters):
                clusters = np.append(clusters, np.zeros((self.n_clusters-nr_objects)*3))

            return [user_temp, clusters.tolist()];

# Classe do controlador
class controller:
    def __init__ (self, inP, inI, Sat_Thres = 1023, Und_Thres = 200):
        self.P = inP
        self.I = inI
        self._TotalI = 0
        self._last_e = 0
        self.ref = 0
        self._Sat_Thres = Sat_Thres
        self._Und_Thres = Und_Thres

    # Controlador PI
    def compute_actuation (self, x):
        e = (self.ref - x)
        self._TotalI += self.I*(e + self._last_e)/2
        y = self.P*e + self._TotalI
        self._last_e = e

        #Sistema Anti-Windup
        if (y > self._Sat_Thres):
            y = self._Sat_Thres
            self._TotalI -= self.I*(e + self._last_e)/2

        if (y < self._Und_Thres):
            y = self._Und_Thres
            self._TotalI -= self.I*(e + self._last_e)/2

        return y
