# RasPagerC9000: Java

## Run
* Install [Wiring Pi](http://wiringpi.com/download-and-install/)
* Create a configfile (using the example below)
* Run `root@raspberrypi:~# java -jar RasPagerC9000.jar -configfile=config.txt`

### Use Serial Port
You may need to prepeare the serial port interface first. There are two options available:

#### Simple
1. Use `raspi-config` to disable the console on the serial port.
2. Set `enable_uart=1` in `/boot/config.txt`
3. Reboot.

#### Advanced
1. Remove the `console=ttyAMA0,115200` and `kgdboc=ttyAMA0,115200` configuration parameters from the `/boot/cmdline.txt` configuration file.
2. Edit the `/etc/inittab` file and comment out the use of the `ttyAMA0` serial port.
3. Reboot.

## Example-Configuration
```
#[slave config]
# Port
port=1337

# Allowed Masters, separated by a space
master=127.0.0.1

# Baudrate (50, 75, 110, 134, 150, 200, 300, 600, 1200, 1800, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400)
baudrate=57600

# LogLevel
# NORMAL = 0; DEBUG_CONNECTION = 1; DEBUG_SENDING = 2; DEBUG_TCP = 3;
loglevel=0
```

## Build
* Java JDK 1.8
* Libraries
	* [Pi4J](http://pi4j.com/)
		* `pi4j-core.jar`
		* `pi4j-device.jar`
		* `pi4j-gpio-extension.jar`
		* `pi4j-service.jar`
