package de.rwth_aachen.afu.raspager.c9000;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.*;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.rwth_aachen.afu.raspager.Configuration;
import de.rwth_aachen.afu.raspager.Transmitter;


/**
 * Klasse zum Senden der Daten ber die serielle Schnittstelle des Raspberry Pis
 *
 * @author menzerath, Wilke
 */

public final class C9000Transmitter implements Transmitter {
	private static final Logger log = Logger.getLogger(C9000Transmitter.class.getName());
	private final Object lockObj = new Object();
	private Serial serial;
//	private int txDelay = 0;

	private GpioController gpio;
	private GpioPinDigitalOutput pinAvr;
	private GpioPinDigitalOutput pinPtt;
	private GpioPinDigitalInput pinSenddata;


	@Override
	public void close() throws Exception {
		synchronized (lockObj) {
			try {
				if (serial != null) {
					serial.close();
					serial = null;
				}
			} catch (Throwable t) {
				log.log(Level.SEVERE, "Failed to close serial port.", t);
			}

/*			try {
				if (gpio != null) {
					gpio.close();
					gpio = null;
				}
			} catch (Throwable t) {
				log.log(Level.SEVERE, "Failed to close GPIO port.", t);
			}
*/
		}
	}

	@Override
	public void init(Configuration config) throws Exception {
		synchronized (lockObj) {
			close();
//			txDelay = config.getInt("txDelay", 0);

			System.out.println("Initialisiere C9000Communication...");

			this.serial = SerialFactory.createInstance();
			try {
				serial.open(new SerialConfig().device(SerialPort.getDefaultPort()).baud(Baud._38400).parity(Parity.NONE).stopBits(StopBits._1));
			} catch (UnsupportedBoardType | IOException | InterruptedException e) {
				e.printStackTrace();
			}
			this.gpio = GpioFactory.getInstance();

			this.pinAvr = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "AVR_RasPi", PinState.HIGH);
			this.pinAvr.setShutdownOptions(true, PinState.LOW);

			this.pinPtt = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "PTT_RasPi", PinState.LOW);
			this.pinPtt.setShutdownOptions(true, PinState.LOW);

			this.pinSenddata = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, "SENDDATA", PinPullResistance.PULL_DOWN);
			this.pinSenddata.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);

			System.out.println("DataSender initialisiert.");

		}
	}


	/**
	 * Sendet die uebergebenen Daten ueber die serielle Schnittstelle.
	 * Setzt dabei die passenden Pins und wartet auf einen freien Buffer an der Gegenstelle.
	 *
	 * @param inputData zu bertragende Daten
	 */

	public void send(byte[] inputData) {
		synchronized (lockObj) {
			if (serial == null && gpio == null) {
				throw new IllegalStateException("Not initialized");
			}

//			System.out.println("Sende Daten...");

			// PTT (Pin 13, bzw 2) auf HIGH
			this.pinPtt.high();

			// Erstmal kein TX Delay.
/*			if (txDelay > 0) {
				try {
					Thread.sleep(txDelay);
				} catch (Throwable t) {
					log.log(Level.SEVERE, "Failed to wait for TX delay.", t);
				}
			}
*/

			// Warte 1ms
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// durch alle Bytes im Array loopen
			for (int i = 0; i < inputData.length; i++) {
				// SENDDATA (Pin 15, bzw 3) auslesen
				if (this.pinSenddata.isHigh()) {
					// HIGH -> Naechstes Byte senden
//					System.out.println("Sende naechstes Byte...");

					try {
						this.serial.write(inputData[i]);
					} catch (IllegalStateException | IOException e) {
						e.printStackTrace();
					}
				} else {
					// LOW -> Buffer voll -> Warten und erneut versuchen
//					System.out.println("Buffer voll. Warte...");

					// Schritt wiederholen
					i--;

					// Warte 1ms
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			// PTT auf LOW
			this.pinPtt.low();

//			System.out.println("Daten gesendet.");

		}
	}

	/**
	 * Wandelt die bergebene ArrayList mit Integern in ein Byte-Array um, um es anschlieend der send-Funktion zu bergeben.
	 *
	 * @param inputData zu bertragende Daten
	 */
	public void send(List<Integer> inputData) {
		send(getByteData(inputData));
	}

	/**
	 * Wandelt eine bergebene ArrayList mit Integern in ein Byte-Array um und gibt dieses zurck.
	 *
	 * @param data ArrayList mit Integer-Werten
	 * @return Integer-Werte in Byte-Array
	 */
	private static byte[] getByteData(List<Integer> data) {
		byte[] byteData = new byte[data.size() * 4];

		for (int i = 0; i < data.size(); i++) {
			for (int c = 0; c < 4; c++) {
				byteData[i * 4 + c] = (byte) (data.get(i) >>> (8 * (3 - c)));
			}
		}

		return byteData;
	}
}
