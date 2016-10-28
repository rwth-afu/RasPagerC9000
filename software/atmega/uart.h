#pragma once

#include <stdint.h>

/**
 * Atmega8 UART Driver
 *
 * @author Thomas Gatzweiler, DL2IC
 */
class UART {
public:
  /**
   * Initializes the UART interface.
   */
  static void init();

  /**
   * Waits until a byte is received and returns it.
   */
  static uint8_t receive_byte();
};
