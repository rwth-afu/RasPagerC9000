#pragma once

/**
 * First in/first out circular buffer
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
  unsigned int count();

  /**
   * Returns the maximum number of elements that can be stored in the buffer.
   */
  unsigned int size();

  /**
   * Returns the number of slots remaining until the buffer is full.
   */
  unsigned int free_slots();

  /**
   * Returns `true` if the buffer is full.
   */
  bool full();

  /**
   * Returns `true` if the buffer is empty.
   */
  bool empty();

  /**
   * Appends a new element at the end of the buffer.
   */
  void push(T value);

  /**
   * Returns the first element and removes it from the start of the buffer.
   */
  T pop();

private:
  T* _buffer;
  unsigned int _size, _head, _tail;
};
