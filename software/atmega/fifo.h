#pragma once

#include <stdint.h>

/**
 * First in/first out circular buffer
 * Thread-safe for a single producer/single consumer scenario
 *
 * @author Thomas Gatzweiler, DL2IC
 */
template<typename T>
class Fifo {
public:
  /**
   * Creates a new Fifo buffer which can hold `size` elements.
   */
  Fifo(unsigned int size);
  ~Fifo();

  /**
   * Returns the number of elements that are currently stored in the buffer.
   */
  unsigned int getCount();

  /**
   * Returns the maximum number of elements that can be stored in the buffer.
   */
  unsigned int getSize();

  /**
   * Returns the number of slots remaining until the buffer is full.
   */
  unsigned int getFreeSlots();

  /**
   * Returns `true` if the buffer is full.
   */
  bool isFull();

  /**
   * Returns `true` if the buffer is empty.
   */
  bool isEmpty();

  /**
   * Appends a new element at the end of the buffer.
   */
  void push(T value);

  /**
   * Returns the first element and removes it from the start of the buffer.
   */
  T pop();

private:
  T* buffer;
  uint16_t size;
  volatile uint16_t head;
  volatile uint16_t tail;
};
