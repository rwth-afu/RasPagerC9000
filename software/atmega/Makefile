MMCU=atmega8

CXX = avr-g++
CXXFLAGS = -O2 -mmcu=$(MMCU) -Wall -std=c++0x

LD = avr-g++
LDFLAGS = -O2 -mmcu=$(MMCU) -Wall

OBJCOPY = avr-objcopy

AVRDUDE = avrdude
AVRDUDEFLAGS = -C +./avrdude.conf -c raspberry_pi -p m8

MAIN = raspager
OBJ = main.o uart.o fifo.o

all: $(MAIN).hex

$(MAIN).hex: $(MAIN).elf
	$(OBJCOPY) -O ihex $^ $@

$(MAIN).elf: $(OBJ)
	$(LD) $(LDFLAGS) -o $@ $^

%.o: %.c
	$(CXX) $(CFLAGS) -o $@ -c $^

%.o: %.cpp
	$(CXX) $(CXXFLAGS) -o $@ -c $^

clean:
	rm -r $(OBJ) $(MAIN).elf $(MAIN).hex

program: $(MAIN).hex
	sudo $(AVRDUDE) $(AVRDUDEFLAGS) -U flash:w:$(MAIN).hex

fuses:
	sudo $(AVRDUDE) $(AVRDUDEFLAGS) -U lfuse:w:0xff:m -U hfuse:w:0xc9:m

test:
	sudo $(AVRDUDE) $(AVRDUDEFLAGS) -v

start:
	sudo bash -c "echo 17 > /sys/class/gpio/export"
	sudo bash -c "echo out > /sys/class/gpio/gpio17/direction"
	sudo bash -c "echo 0 > /sys/class/gpio/gpio17/value; sleep 1"
	sudo bash -c "echo 1 > /sys/class/gpio/gpio17/value"

stop:
	sudo bash -c "echo 0 > /sys/class/gpio/gpio17/value"
	sudo bash -c "echo 17 > /sys/class/gpio/unexport"

install_dependencies:
	sudo apt-get install avrdude gcc-avr binutils-avr avr-libc

.PHONY: all clean program fuses start stop test install_dependencies
