#pragma once

// CPU Frequency
#define F_CPU 8000000

// Recommended values: 9600, 19200, 38400, 76800
#define UART_BAUDRATE 38400

// Invert each bit before transmitting
#define INVERT_BITS true

// LED_RED -> This pin drives the red LED
#define LED_RED_DDR         DDRB
#define LED_RED_PORT        PORTB
#define LED_RED_PIN         PINB
#define LED_RED_BIT         PINB0

// LED_YELLOW -> This pin drives the yellow LED
#define LED_YELLOW_DDR      DDRB
#define LED_YELLOW_PORT     PORTB
#define LED_YELLOW_PIN      PINB
#define LED_YELLOW_BIT      PINB1

// LED_GREEN -> This pin drives the green LED
#define LED_GREEN_DDR       DDRB
#define LED_GREEN_PORT      PORTB
#define LED_GREEN_PIN       PINB
#define LED_GREEN_BIT       PINB2

// C9000_PTT -> This pin drives the PTT line of C9000, High means C9000 is set to Ground = Low
#define C9000_PTT_DDR       DDRD
#define C9000_PTT_PORT      PORTD
#define C9000_PTT_PIN       PIND
#define C9000_PTT_BIT       PIND5

// C9000_MDL -> This pin drives the MDL line of C9000, High means C9000 MDL is set to Ground = Low
#define C9000_MDL_DDR       DDRD
#define C9000_MDL_PORT      PORTD
#define C9000_MDL_PIN       PIND
#define C9000_MDL_BIT       PIND6

// C9000_MDE -> This pin drives the MDE line of C9000, High means C9000 MDE is set to Ground = Low
#define C9000_MDE_DDR       DDRD
#define C9000_MDE_PORT      PORTD
#define C9000_MDE_PIN       PIND
#define C9000_MDE_BIT       PIND7

// RASPI_SENDATA -> This pin signals the Raspberry Pi that data can be received
#define RASPI_SENDDATA_DDR  DDRC
#define RASPI_SENDDATA_PORT PORTC
#define RASPI_SENDDATA_PIN  PINC
#define RASPI_SENDDATA_BIT  PINC0

// RASPI_PTT -> This pin goes to High if the Raspberry Pi wants to send data
#define RASPI_PTT_DDR       DDRC
#define RASPI_PTT_PORT      PORTC
#define RASPI_PTT_PIN       PINC
#define RASPI_PTT_BIT       PINC1

// Bit Manipulation
#define SET_BIT(var, bitnum)   (var) |=  (1 << (bitnum))
#define CLR_BIT(var, bitnum)   (var) &= ~(1 << (bitnum))
#define TGL_BIT(var, bitnum)   (var) ^=  (1 << (bitnum))
#define GET_BIT(var, bitnum)   (((var) >> (bitnum)) & 0x1)

// IO Defines
#define INIT_OUTPUT(signal)  {SET_BIT(signal##_DDR,  signal##_BIT);}
#define SET_OUTPUT(signal)   {SET_BIT(signal##_PORT, signal##_BIT);}
#define CLR_OUTPUT(signal)   {CLR_BIT(signal##_PORT, signal##_BIT);}
#define GET_INPUT(signal)    (GET_BIT(signal##_PIN,  signal##_BIT))
