
#include "config.h"
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <stdint.h>

#include "uart.h"
#include "fifo.h"

extern void panic();

Fifo<uint8_t> fifo = Fifo<uint8_t>(256);
static bool reception_ok = true;

void timer_init() {
/*  // Initialize timer for 1200 Hz
  TCCR2 = (1 << WGM21) | (1 << CS22);
  OCR2  = F_CPU / 64 / 1207;
  TIMSK = (1 << OCIE2) | (1 << TOIE2);
*/
  // Initialize timer 1 for 1200 Hz
  // No Output on compare match, no PWM
  TCCR1A = 0x00;
  // Clear Timer on match, CPU-Clock as input
  TCCR1B = (1 << WGM12) | (1 << CS10);
  OCR1A = 6666;
  TIMSK = (1 << OCIE1A);
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
  uint16_t free_slots = fifo.getFreeSlots();
  uint16_t count = fifo.getCount();

  // Stop receiving if the buffer is full
  if (!free_slots) {
    CLR_OUTPUT(RASPI_SENDDATA);
    panic();
  }
  else {
	if (reception_ok) {
		SET_OUTPUT(RASPI_SENDDATA);
		uint8_t received_byte = UART::receive_byte();
		fifo.push(received_byte);
		
		if (count >= 125) {
			// Signal the PI to stop sending data if the buffer gets close to full
			CLR_OUTPUT(RASPI_SENDDATA);
		}
		if (count >= 160) {
			reception_ok = false;
		}
	} else {
		if (count <= 120) {
			// if it the count is 120 or below, enable reception again
			reception_ok = true
			SET_OUTPUT(RASPI_SENDDATA);
		}
    }
  }
}

/**
 * 1200 Hz timer interrupt
 */
ISR(TIMER1_COMPA_vect) {
  static uint8_t current_byte = 0;
  static uint8_t remaining_bits = 0;

  bool fifo_empty = fifo.isEmpty();

  // Set PTT to High if there is data left
  // or the Raspberry Pi wants to send
  if (remaining_bits || !fifo_empty || GET_INPUT(RASPI_PTT)) {
    SET_OUTPUT(LED_RED);
    SET_OUTPUT(C9000_PTT);
  }
  else {
    CLR_OUTPUT(LED_RED);
    CLR_OUTPUT(C9000_PTT);
  }

  if (remaining_bits == 0 && !fifo_empty) {
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

    for (int i = 0; i < 8; i++) asm volatile("nop");
    SET_OUTPUT(C9000_MDE);
    for (int i = 0; i < 8; i++) asm volatile("nop");
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

void panic() {
  INIT_OUTPUT(LED_RED);
  INIT_OUTPUT(LED_YELLOW);
  INIT_OUTPUT(LED_GREEN);

  for (int i = 0; i < 100; i++) {
    SET_OUTPUT(LED_RED);
    SET_OUTPUT(LED_YELLOW);
    SET_OUTPUT(LED_GREEN);
    _delay_ms(50);
    CLR_OUTPUT(LED_RED);
    CLR_OUTPUT(LED_YELLOW);
    CLR_OUTPUT(LED_GREEN);
    _delay_ms(50);
  }
}
