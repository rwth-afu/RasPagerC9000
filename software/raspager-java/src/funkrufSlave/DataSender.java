package funkrufSlave;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Klasse zum Senden der Daten über die serielle Schnittstelle des Raspberry Pis
 *
 * @author menzerath
 */
public class DataSender {
    private Serial serial;
    private GpioController gpio;
    private GpioPinDigitalOutput pinPtt;
    private GpioPinDigitalInput pinSenddata;

    /**
     * Konstruktor.
     * Initialisiert die serielle Schnittstelle mit passenden Konfigurationswerten, die GPIO-Schnittstelle und die verwendeten GPIO-Pins.
     */
    public DataSender() {
        System.out.println("Initialisiere DataSender...");

        this.serial = SerialFactory.createInstance();
        try {
            serial.open(new SerialConfig().device(SerialPort.getDefaultPort()).baud(Baud._57600).parity(Parity.NONE).stopBits(StopBits._1));
        } catch (UnsupportedBoardType | IOException | InterruptedException e) {
            e.printStackTrace();
        }

        this.gpio = GpioFactory.getInstance();
        this.pinPtt = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "PTT_RasPi", PinState.LOW);
        this.pinSenddata = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, "SENDDATA", PinPullResistance.PULL_DOWN);

        System.out.println("DataSender initialisiert.");
    }

    /**
     * Sendet die übergebenen Daten über die serielle Schnittstelle.
     * Setzt dabei die passenden Pins und wartet auf einen freien Buffer an der Gegenstelle.
     *
     * @param inputData zu übertragende Daten
     */
    public void send(byte[] inputData) {
        System.out.println("Sende Daten...");

        // PTT (Pin 13, bzw 2) auf HIGH
        this.pinPtt.high();

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
                // HIGH -> Nächstes Byte senden
                System.out.println("Sende nächstes Byte...");

                try {
                    this.serial.write(inputData[i]);
                } catch (IllegalStateException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                // LOW -> Buffer voll -> Warten und erneut versuchen
                System.out.println("Buffer voll. Warte...");

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

        System.out.println("Daten gesendet.");
    }

    /**
     * Wandelt die übergebene ArrayList mit Integern in ein Byte-Array um, um es anschließend der send-Funktion zu übergeben.
     *
     * @param inputData zu übertragende Daten
     */
    public void send(ArrayList<Integer> inputData) {
        send(getByteData(inputData));
    }

    /**
     * Wandelt eine übergebene ArrayList mit Integern in ein Byte-Array um und gibt dieses zurück.
     *
     * @param data ArrayList mit Integer-Werten
     * @return Integer-Werte in Byte-Array
     */
    public static byte[] getByteData(ArrayList<Integer> data) {
        byte[] byteData = new byte[data.size() * 4];

        for (int i = 0; i < data.size(); i++) {
            for (int c = 0; c < 4; c++) {
                byteData[i * 4 + c] = (byte) (data.get(i) >>> (8 * (3 - c)));
            }
        }

        return byteData;
    }
}