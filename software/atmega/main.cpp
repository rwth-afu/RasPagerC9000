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

  INIT_OUTPUT(C9000_MDL);
  SET_OUTPUT(C9000_MDL);

  INIT_OUTPUT(C9000_MDE);
  CLR_OUTPUT(C9000_MDE);

  INIT_OUTPUT(C9000_PTT);
  CLR_OUTPUT(C9000_PTT);

  INIT_OUTPUT(RASPI_SENDDATA);
  SET_OUTPUT(RASPI_SENDDATA);
}

/**
 * Main loop
 */
void loop() {
  // Stop receiving if the buffer is full
  if (fifo.full()) {
    CLR_OUTPUT(RASPI_SENDDATA);
  }
  else {
    uint8_t received_byte = UART::receive_byte();
    fifo.push(received_byte);
  }

  // Signal the PI to stop sending data if the buffer gets close to full
  if (fifo.free_slots() < 16) {
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
  static uint8_t current_byte = 0;
  static uint8_t remaining_bits = 0;

  // Set PTT to High if there is data left
  // or the Raspberry Pi wants to send
  if (remaining_bits || GET_INPUT(RASPI_PTT) || !fifo.empty()) {
    SET_OUTPUT(LED_RED);
    SET_OUTPUT(C9000_PTT);
  }
  else {
    CLR_OUTPUT(LED_RED);
    CLR_OUTPUT(C9000_PTT);
  }

  if (remaining_bits == 0 && !fifo.empty()) {
    current_byte = fifo.pop();
    remaining_bits = 8;
  }

  if (remaining_bits > 0) {
    uint8_t bit = current_byte & (1 << (remaining_bits - 1));

    if ((!INVERT_BITS && bit) || (INVERT_BITS && !bit)) {
      CLR_OUTPUT(C9000_MDL);
      SET_OUTPUT(LED_YELLOW);
    } else {
      SET_OUTPUT(C9000_MDL);
      CLR_OUTPUT(LED_YELLOW);
    }

    SET_OUTPUT(C9000_MDE);
    asm volatile("nop"); // short delay
    CLR_OUTPUT(C9000_MDE);

    remaining_bits--;
  }
  else {
    SET_OUTPUT(C9000_MDL);
    CLR_OUTPUT(LED_YELLOW);
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
