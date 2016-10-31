# RasPagerC9000
Adapted Version of RasPager to generate Signals for Ericsson C9000 Paging Transmitter

Authors:
* Ralf Wilke DH3WR, Aachen
* Michael Delissen, Aachen
* Marvin Menzerath, Aachen
* Christian Jansen, Aachen
* Philipp Thiel, Aachen
* Thomas Gatzweiler

This software is released free of charge under the Creative Commons License of type "by-nc-sa". No commercial use is allowed. The software licenses of the used libs apply in any case.

## Run
* Install [Wiring Pi](http://wiringpi.com/download-and-install/)
* `root@raspberrypi:~# java -jar RasPagerC9000.jar`

### Use Serial Port

##### Simple
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

# Allowed Masters, seperated by a space
master=127.0.0.1

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
