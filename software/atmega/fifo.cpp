#include "fifo.h"
#include <stdlib.h>
#include <util/atomic.h>

extern void panic();

template <typename T>
Fifo<T>::Fifo(uint16_t size) {
  this->buffer = (T*)malloc(size * sizeof(T));
  if (!buffer) panic();
  this->size = size;
  this->head = this->tail = 0;
}

template <typename T>
Fifo<T>::~Fifo() {
  free(this->buffer);
}

template <typename T>
uint16_t Fifo<T>::getCount() {
  uint16_t current_head, current_tail;
  ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
    current_head = this->head;
    current_tail = this->tail;
  }

  if (current_head < current_tail) {
    return (current_head + this->size) - current_tail;
  }
  else {
    return current_head - current_tail;
  }

}

template <typename T>
uint16_t Fifo<T>::getSize() {
  return this->size;
}

template <typename T>
uint16_t Fifo<T>::getFreeSlots() {
  return this->size - this->getCount() - 1;
}

template <typename T>
bool Fifo<T>::isFull() {
  uint16_t current_head, current_tail;
  ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
    current_head = this->head;
    current_tail = this->tail;
  }

  return ((current_head + 1) % this->size) == current_tail;
}

template <typename T>
bool Fifo<T>::isEmpty() {
  bool result;
  ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
    result = this->head == this->tail;
  }

  return result;
}

template <typename T>
void Fifo<T>::push(T value) {
  if (!this->isFull()) {
    this->buffer[this->head] = value;
    ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
      this->head = (this->head + 1) % this->size;
    }
  }
  else {
    // ERROR: Full buffer
    panic();
  }
}

template <typename T>
T Fifo<T>::pop() {
  if (!this->isEmpty()) {
    T value = this->buffer[this->tail];
    ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
      this->tail = (this->tail + 1) % this->size;
    }
    return value;
  }
  else {
    // ERROR: empty buffer
    panic();
    return 0;
  }
}

template class Fifo<uint8_t>;
