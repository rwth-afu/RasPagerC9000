#include "config.h"
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <stdint.h>

#include "uart.h"
#include "fifo.h"

Fifo<uint8_t> fifo = Fifo<uint8_t>(512);

void timer_init() {
  // Initialize timer for 1200 Hz
	TCCR2 = (1 << WGM21) | (1 << CS22);
	OCR2  = F_CPU / 64 / 1207;
	TIMSK = (1 << OCIE2) | (1 << TOIE2);
}

void ports_init() {
  INIT_OUTPUT(LED_RED);
  CLR_OUTPUT(LED_RED);

  INIT_OUTPUT(LED_YELLOW);
  CLR_OUTPUT(LED_YELLOW);

  INIT_OUTPUT(LED_GREEN);
  SET_OUTPUT(LED_GREEN);

  INIT_OUTPUT(MDL_C9000);
  SET_OUTPUT(MDL_C9000);

  INIT_OUTPUT(MDE_C9000);
  SET_OUTPUT(MDE_C9000);

  INIT_OUTPUT(RASPI_SENDDATA);
  SET_OUTPUT(RASPI_SENDDATA);
}

/**
 * Main loop
 */
void loop() {
  // Cancel the reception immediately if the buffer is full
  if (fifo.full()) {
    CLR_OUTPUT(RASPI_SENDDATA);
    return;
  }

  uint8_t received_byte = UART::receive_byte();
  fifo.push(received_byte);

  // Signal the PI to stop sending data if the buffer gets close to full
  if (fifo.free_slots() < 32) {
    CLR_OUTPUT(RASPI_SENDDATA);
  }
  else {
    SET_OUTPUT(RASPI_SENDDATA);
  }
}

/**
 * 1200 Hz timer interrupt
 */
ISR(TIMER2_COMP_vect) {
  static uint8_t byte = 0;
  static uint8_t remaining_bits = 0;

  if (remaining_bits == 0 && !fifo.empty()) {
    byte = fifo.pop();
    remaining_bits = 8;
  }

  if (remaining_bits > 0) {
    uint8_t bit = byte & (1 << remaining_bits);
    if (bit) { CLR_OUTPUT(MDL_C9000); } else { SET_OUTPUT(MDL_C9000); }
    remaining_bits--;
  }
  else {
    SET_OUTPUT(MDL_C9000);
  }
}

int main() {
  ports_init();
  timer_init();
  UART::init();
  sei();

  while (1) { loop(); }
  return 0;
}
