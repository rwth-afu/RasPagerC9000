#include "fifo.h"
#include <stdint.h>
#include <stdlib.h>

template <typename T>
Fifo<T>::Fifo(unsigned int size) {
  _buffer = (T*)malloc(size * sizeof(T));
  _size = size;
  _head = _tail = 0;
}

template <typename T>
Fifo<T>::~Fifo() {
  free(_buffer);
}

template <typename T>
unsigned int Fifo<T>::count() {
  if (_head <= _tail) {
    return (_head + _size) - _tail;
  }
  else {
    return _head - _tail;
  }
}

template <typename T>
unsigned int Fifo<T>::size() {
  return _size;
}

template <typename T>
unsigned int Fifo<T>::free_slots() {
  return _size - count() - 1;
}

template <typename T>
bool Fifo<T>::full() {
  return ((_head + 1) % _size) == _tail;
}

template <typename T>
bool Fifo<T>::empty() {
  return _head == _tail;
}

template <typename T>
void Fifo<T>::push(T value) {
  unsigned int next = _head + 1 % _size;

  if (next == _tail) {
    // ERROR: Full buffer
  }
  else {
    _buffer[_head] = value;
    _head = next;
  }
}

template <typename T>
T Fifo<T>::pop() {
  if (!empty()) {
    T value = _buffer[_tail];
    _tail = _tail + 1 % _size;
    return value;
  }
  else {
    // ERROR: empty buffer
    return 0;
  }
}

template class Fifo<uint8_t>;
