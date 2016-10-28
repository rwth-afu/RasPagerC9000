#pragma once

// CPU Frequency
#define F_CPU 8000000

#define UART_BAUDRATE 38400

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

// PTT_C9000 -> This pin drives the PTT line of C9000, High means C9000 is set to Ground = Low
#define PTT_C9000_DDR       DDRD
#define PTT_C9000_PORT      PORTD
#define PTT_C9000_PIN       PIND
#define PTT_C9000_BIT       PIND5

// MDL_C9000 -> This pin drives the MDL line of C9000, High means C9000 MDL is set to Ground = Low
#define MDL_C9000_DDR       DDRD
#define MDL_C9000_PORT      PORTD
#define MDL_C9000_PIN       PIND
#define MDL_C9000_BIT       PIND6

// MDE_C9000 -> This pin drives the MDE line of C9000, High means C9000 MDE is set to Ground = Low
#define MDE_C9000_DDR       DDRD
#define MDE_C9000_PORT      PORTD
#define MDE_C9000_PIN       PIND
#define MDE_C9000_BIT       PIND7

// RASPI_SENDATA -> This pin drives the MDE line of C9000, High means C9000 MDL is set to Ground = Low
#define RASPI_SENDDATA_DDR   DDRC
#define RASPI_SENDDATA_PORT  PORTC
#define RASPI_SENDDATA_PIN   PINC
#define RASPI_SENDDATA_BIT   PINC0

// RASPI_PTT -> This pin drives the MDE line of C9000, High means C9000 MDL is set to Ground = Low
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
