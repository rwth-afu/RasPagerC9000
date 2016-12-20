package de.rwth_aachen.afu.raspager;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class Scheduler extends TimerTask {
	protected enum State {
		AWAITING_SLOT, SLOT_STILL_ALLOWED
	}

	private static final Logger log = Logger.getLogger(Scheduler.class.getName());
	// max time value (2^16)
	protected static final int MAX = 65536;
	protected static final int MAX_ENCODE_TIME_100MS = 3;
	protected static final int TIMERCYCLE_MS = 10;

	protected AtomicBoolean canceled = new AtomicBoolean(false);
	protected final TimeSlots slots = new TimeSlots();
	protected final Deque<Message> messageQueue;
	protected final Transmitter transmitter;

	protected int time = 0;
	protected int delay = 0;
	protected Consumer<TimeSlots> updateTimeSlotsHandler;
	protected State schedulerState = State.AWAITING_SLOT;
	protected List<Integer> codeWords;

	public Scheduler(Deque<Message> messageQueue, Transmitter transmitter) {
		this.messageQueue = messageQueue;
		this.transmitter = transmitter;
	}

	public void setUpdateTimeSlotsHandler(Consumer<TimeSlots> handler) {
		updateTimeSlotsHandler = handler;
	}

	@Override
	public boolean cancel() {
		canceled.set(true);

		return super.cancel();
	}

	@Override
	public void run() {
		if (canceled.get()) {
			return;
		}
		// Once for all calculate where we are in time now and
		// use this in all actions performed in this timer run
		time = ((int) (System.currentTimeMillis() / 100) + delay) % MAX;

		if (slots.hasChanged(time) && updateTimeSlotsHandler != null) {
			// log.fine("Updating time slots.");
			updateTimeSlotsHandler.accept(slots);
		}

		switch (schedulerState) {
		case AWAITING_SLOT:
			sendData();
			break;
		case SLOT_STILL_ALLOWED:
			stillAllowed();
			break;
		default:
			log.log(Level.WARNING, "Unknown state {0}.", schedulerState);
		}
	}

	private void sendData() {
		if (slots.isAllowed(time) && !messageQueue.isEmpty()) {
			int currentSlot = TimeSlots.getIndex(time);
			int count = slots.getCount(currentSlot);

			if (updateData(count, true)) {
				log.fine("Activating transmitter.");
				try {
					transmitter.send(codeWords);
					log.fine("Data sent");
				} catch (Throwable t) {
					log.log(Level.SEVERE, "Failed to send data.", t);
				} finally {
					schedulerState = State.SLOT_STILL_ALLOWED;
				}

				log.log(Level.FINE, "state = {0}", schedulerState);
			}
		}
	}

	private void stillAllowed() {
		try {
			if (slots.isAllowed(time)) {
				if (!messageQueue.isEmpty()) {
					int currentSlot = TimeSlots.getIndex(time);
					int count = slots.getCount(currentSlot);
					if (updateData(count, false)) {
						log.fine("Activating transmitter.");
						try {
							transmitter.send(codeWords);
							log.fine("Data sent");
						} catch (Throwable t) {
							log.log(Level.SEVERE, "Failed to send data.", t);
						} finally {
							schedulerState = State.SLOT_STILL_ALLOWED;
						}

						log.log(Level.FINE, "state = {0}", schedulerState);
					}
				}
			} else {
					schedulerState = State.AWAITING_SLOT;
					log.log(Level.FINE, "state = {0}", schedulerState);
			}
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Failed to encode data.", t);
			schedulerState = State.AWAITING_SLOT;
		}
	}

	/**
	 * Gets data depending on the given slot count.
	 * 
	 * @param slotCount
	 *            Slot count.
	 * @return Code words to send.
	 */
	private boolean updateData(int slotCount, boolean actualSlotRowIsComplete) {
		if (slotCount <= 0) {
			log.warning("Called updateData with slotCount <= 0.");
			return false;
		}
		int maxBatch = 0;

		if (actualSlotRowIsComplete) {
			// Number of batches per complete slot:
			// ((slotCount * slot time[s]) - praeambel time[s] - txdelay [s]) / bps / ((frames + (1 = sync)) * bits per frame)
			// Example: ((n x 6.4) - 0.48 - 0) * 1200 / ((16 + 1) * 32)
			maxBatch = (int) ((6.40 * slotCount) - 0.48) * 1200 / 544;
			log.log(Level.FINE, "Actual slot complete, Count: {0}", slotCount);
			// If there isn't space for a single batch left in this time slot row, quit with false
			if (maxBatch <= 0) {
				log.log(Level.SEVERE,"No more batches are fitting although it's a complete slot, Value: {0}", maxBatch);
				return false;
			}
		} else {
			// If first slot is not complete any more, because there was a transmission already in this slot
			// (((slotCount - 1) * slot time[s]) + Time_left_in_this_slot[s]- praeambel time[s] - txdelay [s]) / bps / ((frames + (1 = sync)) * bits per frame)

			int timeLeftInThisSlot_100MS = slots.getTimeToNextSlot(time);
			maxBatch = (int) ((6.40 * (slotCount - 1)) + (timeLeftInThisSlot_100MS / 10) - 0.48) * 1200 / 544;
			log.log(Level.FINE, String.format("Actual slot incomplete, Count: %1$d, Time left in 0.1s: %2$d",
					slotCount, timeLeftInThisSlot_100MS));

			// If there isn't space for a single batch left in this time slot row, quit with false
			if (maxBatch <= 0) {
				log.log(Level.FINE, String.format("No more batches are fitting now, Count: %1$d, Time left in 0.1s: %2$d",
						slotCount, timeLeftInThisSlot_100MS));
				return false;
			}

		}

		// send batches

		log.log(Level.FINE, "MaxBatch = {0}", maxBatch);
		int msgCount = 0;

		codeWords = new ArrayList<>();

		// add preambel
		for (int i = 0; i < 18; i++) {
			codeWords.add(Pocsag.PRAEAMBLE);
		}
		// get messages as long as message queue is not empty
		while (!messageQueue.isEmpty()) {
			// get message from queue
			Message message = messageQueue.pop();

			// get codewords and frame position
			List<Integer> cwBuf = message.getCodeWords();
			int framePos = cwBuf.get(0);
			int cwCount = cwBuf.size() - 1;

			// (data.size() - 18) / 17 = aktBatches
			// aktBatches + (cwCount + 2 * framePos) / 16 + 1 = Batches NACH
			// hinzufügen
			// also Batches NACH hinzufügen > maxBatches, dann keine neue
			// Nachricht holen
			// if count of batches + this message is greater than max batches
			if (((codeWords.size() - 18) / 17 + (cwCount + 2 * framePos) / 16 + 1) > maxBatch) {
				messageQueue.addFirst(message);
				break;
			}

			++msgCount;

			// each batch starts with a sync code word
			codeWords.add(Pocsag.SYNC);

			// add idle code words until frame position is reached
			for (int c = 0; c < framePos; c++) {
				codeWords.add(Pocsag.IDLE);
				codeWords.add(Pocsag.IDLE);
			}

			// add actual payload
			for (int c = 1; c < cwBuf.size(); c++) {
				if ((codeWords.size() - 18) % 17 == 0) {
					codeWords.add(Pocsag.SYNC);
				}
				codeWords.add(cwBuf.get(c));
			}

			// fill batch with idle-words
			while ((codeWords.size() - 18) % 17 != 0) {
				codeWords.add(Pocsag.IDLE);
			}
		}

		if (msgCount > 0) {
			log.fine(String.format("Batches used: %1$d / %2$d", ((codeWords.size() - 18) / 17), maxBatch));
			return true;
		} else {
			return false;
		}
	}

	public TimeSlots getSlots() {
		return slots;
	}

	public void setTimeSlots(String s) {
		slots.setSlots(s);
	}

	/**
	 * Gets current time.
	 * 
	 * @return Current time.
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Sets time correction.
	 * 
	 * @param delay
	 *            Time correction.
	 */
	public void correctTime(int delay) {
		this.delay += delay;
	}
}
