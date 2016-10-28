#include "config.h"
#include "uart.h"
#include <avr/io.h>

#define BAUD UART_BAUDRATE
#include <util/setbaud.h>

void UART::init() {
  // Baudrate initialization
  UBRRH = UBRRH_VALUE;
  UBRRL = UBRRL_VALUE;

#if USE_2X
  UCSRA |= (1 << U2X);
#else
  UCSRA &= ~(1 << U2X);
#endif

  // Enable RX
  UCSRB |= (1 << RXEN);

  // Set mode to 8 bit / 1 stopbit / no parity
  UCSRC = (1 << URSEL) | (1 << UCSZ1) | (1 << UCSZ0);
}

uint8_t UART::receive_byte() {
  while (!(UCSRA & (1 << RXC)));
  return UDR;
}
