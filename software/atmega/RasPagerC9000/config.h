

// Frequency
#define F_CPU 8000000

// LED_RED -> This pin drives the red LED
#define LED_RED_PORT      PORTB
#define LED_RED_PIN       PINB
#define LED_RED_BIT       PINB0

// LED_YELLOW -> This pin drives the yellow LED
#define LED_YELLOW_PORT      PORTB
#define LED_YELLOW_PIN       PINB
#define LED_YELLOW_BIT       PINB1

// LED_GREEN -> This pin drives the green LED
#define LED_GREEN_PORT      PORTB
#define LED_GREEN_PIN       PINB
#define LED_GREEN_BIT       PINB2

// PTT_C9000 -> This pin drives the PTT line of C9000, High means C9000 is set to Ground = Low
#define PTT_C9000_PORT      PORTD
#define PTT_C9000_PIN       PIND
#define PTT_C9000_BIT       PIND5

// MDL_C9000 -> This pin drives the MDL line of C9000, High means C9000 MDL is set to Ground = Low
#define MDL_C9000_PORT      PORTD
#define MDL_C9000_PIN       PIND
#define MDL_C9000_BIT       PIND6

// MDE_C9000 -> This pin drives the MDE line of C9000, High means C9000 MDL is set to Ground = Low
#define MDE_C9000_PORT      PORTD
#define MDE_C9000_PIN       PIND
#define MDE_C9000_BIT       PIND7

// RASPI_SENDATA -> This pin drives the MDE line of C9000, High means C9000 MDL is set to Ground = Low
#define RASPI_SENDATA_PORT      PORTC
#define RASPI_SENDATA_PIN       PINC
#define RASPI_SENDATA_BIT       PINC0

// RASPI_PTT -> This pin drives the MDE line of C9000, High means C9000 MDL is set to Ground = Low
#define RASPI_PTT_PORT      PORTC
#define RASPI_PTT_PIN       PINC
#define RASPI_PTT_BIT       PINC1
