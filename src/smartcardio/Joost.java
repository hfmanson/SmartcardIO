package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Joost {
    public final static byte[] AID_JOOSTAPPLET = { (byte) 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 };

    public static void joost(SmartcardIO smartcardIO) throws CardException {

        // Send Select Applet command
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_JOOSTAPPLET);
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            c = new CommandAPDU(0x80, 0x00, 0x00, 0x00, 0x100);
            responseAPDU = smartcardIO.runAPDU(c);
        }
    }


    public static void main(String[] args) {
        SmartcardIO smartcardIO = new SmartcardIO();
        smartcardIO.debug = true;
        try {
            smartcardIO.setup();
            joost(smartcardIO);
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        } finally {
            smartcardIO.teardown();
        }
    }
}
