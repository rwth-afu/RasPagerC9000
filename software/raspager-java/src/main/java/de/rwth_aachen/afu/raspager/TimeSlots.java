package de.rwth_aachen.afu.raspager;

final class TimeSlots {

	private final boolean[] slots = new boolean[16];
	private int lastSlotIndex = -1;
	protected static final int MAX = 65536;

	/**
	 * Sets active slots based on string representation.
	 * 
	 * @param s
	 *            String representation of active slots.
	 */
	public synchronized void setSlots(String s) {
		// Reset all to false (instead of creating a new array)
		for (int i = 0; i < slots.length; ++i) {
			slots[i] = false;
		}

		for (int i = 0; i < s.length(); ++i) {
			int idx = Character.digit(s.charAt(i), 16);
			slots[idx] = true;
		}
	}

	/**
	 * Checks if slot is allowed and counts how many active slots are in a row.
	 * 
	 * @param cs
	 *            Slot to check.
	 * @return Number of active slots.
	 */
	public synchronized int getCount(char cs) {
		return getCount(Character.digit(cs, 16));
	}

	/**
	 * Checks if slot is allowed and counts how many active slots are in a row.
	 * 
	 * @param slot
	 *            Slot to check.
	 * @return Number of active slots.
	 */
	public synchronized int getCount(int slot) {
		int count = 0;

		for (int i = slot; slots[i % 16] && (i < slot + 16); ++i) {
			++count;
		}

		return count;
	}

	/**
	 * Gets active slots as a string.
	 * 
	 * @return String containing active slot indices.
	 */
	public synchronized String getSlots() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < slots.length; ++i) {
			if (slots[i]) {
				sb.append(String.format("%1x", i));
			}
		}

		return sb.toString();
	}

	/**
	 * Gets the value of a slot.
	 * 
	 * @param index
	 *            Slot index (smaller than 16).
	 * @return Status of the slot at the given index.
	 */
	public synchronized boolean get(int index) {
		return slots[index % 16];
	}

	/**
	 * Checks if the current slot is allowed.
	 * 
	 * @param time
	 *            Time
	 * @return True if the slot is allowed.
	 */
	public boolean isAllowed(int time) {
		return get(getIndex(time));
	}

	/**
	 * Checks if the slot has changed.
	 * 
	 * @param time
	 *            Current time
	 * @return True if the given slot number is the last slot.
	 */
	public synchronized boolean hasChanged(int time) {
		int slot = getIndex(time);
		if (lastSlotIndex == slot) {
			return false;
		} else {
			lastSlotIndex = slot;
			return true;
		}
	}

	/**
	 * Cheks if the next slot will be active.
	 * 
	 * @param time
	 *            Time
	 * @return True if the next slot will be active.
	 */
	public synchronized boolean isNextAllowed(int time) {
		return get((getIndex(time) + 1) % 16);
	}

	/**
	 * Gets the current slot for the given time value.
	 * 
	 * @param time
	 *            Time value
	 * @return Current slot as hex number as type char.
	 */
	public static char getCurrentSlotChar(int time) {
		return Character.forDigit(getIndex(time), 16);
	}

	/**
	 * Gets the current slot index for the given time value.
	 * 
	 * @param time
	 *            Time value.
	 * @return Slot index.
	 */
	public static int getIndex(int time) {
		// time (in 0.1s), time per slot 6.4 s = 64 * 0.1s
		// % 16 to warp around complete minutes, as there are 16 timeslots
		// avaliable.

		// **** IMPORTANT ****

		// This means 16 timeslots need 102.4 seconds, not 60.
		return ((int) Math.floor((time % 1024) / 64));
	}

	public static int getStartTimeForSlot(int slot, int time) {
		double startTimeofSlotZero = (Math.floor(time / 1024)) * 1024;
		return (int) ((startTimeofSlotZero + (slot * 64)) % MAX);
	}

	public static int getStartTimeForNextSlot(int time) {
		int TimeInNextSlot = (time + 64) % MAX;
		return TimeInNextSlot - (TimeInNextSlot % 64);
	}

	public static int getEndTimeForSlot(int slot, int time) {
		return ((getStartTimeForSlot(slot, time) + 1023) % MAX);
	}

	// result in 0.1 s units
	public static int getTimeToNextSlot(int time) {
		int StartTimeNext = getStartTimeForNextSlot(time);
		int timedifference =  StartTimeNext - time;

		// If the next slot is after a wrap around, add the MAX value
		if (timedifference < 0) {
			timedifference += MAX;
		}
		return timedifference;
	}
}
