package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Hello {
    public final static byte[] AID_HELLOAPPLET2 = {
            (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x02,
            (byte) 0xFF, 0x49, 0x50, 0x25, (byte) 0x89,
            (byte) 0xC0, 0x01, (byte) 0x9B, 0x01
    };

    public static void hello2(SmartcardIO smartcardIO) throws CardException {

        // Send Select Applet command
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_HELLOAPPLET2);
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            c = new CommandAPDU(0x90, 0x10, 0x00, 0x00, 0x100);
            smartcardIO.runAPDU(c);
        }
    }

    public static void main(String[] args) {
        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            smartcardIO.setup();
            hello2(smartcardIO);
            smartcardIO.teardown();
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        }
    }
}
