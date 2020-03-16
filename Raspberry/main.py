import seeed_mlx90640
import time

def main():
    mlx = seeed_mlx90640.grove_mxl90640()
    frame = [0] * 768
    while True:
        try:
            mlx.getFrame(frame)
        except ValueError:
            continue

if __name__  == '__main__':
    main()
