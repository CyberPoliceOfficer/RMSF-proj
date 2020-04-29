import wiringpi2 as piwiring
import time
pin2 = 13
piwiring.wiringPiSetupGpio()
piwiring.pinMode(pin2, 2)
piwiring.pwmWrite(pin2, 0) # off/ low
delay = 10
piwiring.pwmWrite(pin2, 512) # 50%
time.sleep(delay)
piwiring.pwmWrite(pin2, 0) # off/ low
