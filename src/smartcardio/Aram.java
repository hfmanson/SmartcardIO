package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Aram {
    public final static byte[] AID_ARAM = { (byte) 0xA0, 0x00, 0x00, 0x01, 0x51, 0x41, 0x43, 0x4c, 0x00 };

    public static void aram(SmartcardIO smartcardIO) throws CardException {
        CommandAPDU c;
        ResponseAPDU responseAPDU;
        // Send Select ARA-M Applet
        c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_ARAM);
        responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            // Add 'Allow all' rule
            c = new CommandAPDU(0x80, 0xE2, 0x90, 0x00, new byte[] {
                (byte) 0xF0, (byte) 0x0D, (byte) 0xE2, (byte) 0x0B, (byte) 0xE1, (byte) 0x04, (byte) 0x4F, (byte) 0x00, (byte) 0xC1,
                (byte) 0x00, (byte) 0xE3, (byte) 0x03, (byte) 0xD0, (byte) 0x01, (byte) 0x01, (byte) 0xF4, (byte) 0xDE, (byte) 0xA2, (byte) 0x45, (byte) 0x76, (byte) 0x84, (byte) 0x85, (byte) 0x06
            });
            responseAPDU = smartcardIO.runAPDU(c);
            if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
                // Show ARA rules
                c = new CommandAPDU(0x80, 0xCA, 0xFF, 0x40, 0x100);
                responseAPDU = smartcardIO.runAPDU(c);
                if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            smartcardIO.setup();
            aram(smartcardIO);
            smartcardIO.teardown();
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        }
    }
}
