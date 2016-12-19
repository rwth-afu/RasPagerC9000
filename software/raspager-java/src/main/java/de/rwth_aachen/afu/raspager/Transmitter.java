package de.rwth_aachen.afu.raspager;

import java.util.List;

public interface Transmitter extends AutoCloseable {

	/**
	 * Initializes the transmitter.
	 * 
	 * @param config
	 *            Handle to the configuration file.
	 * @throws Exception
	 *             If an error occurred during initialization.
	 */
	void init(Configuration config) throws Exception;


	/**
	 * Sends the encoded data over the air.
	 * 
	 * @param data
	 *            Data to send.
	 * @throws Exception
	 *             If an error occurred while sending the data.
	 */
	void send(List<Integer> data) throws Exception;
}
