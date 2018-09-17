package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class IsoApplet {
    public final static byte[] AID_ISOAPPLET = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };

    public static void testIsoApplet(SmartcardIO smartcardIO) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_ISOAPPLET);
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == 0x9000) {
            //00A40800045015440400
            c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { 0x50, 0x15, 0x44, 0x04 });
            responseAPDU = smartcardIO.runAPDU(c);
            if (responseAPDU.getSW() == 0x9000) {
                c = new CommandAPDU(0x00, 0xB0, 0x00, 0x00, 0x100);
                responseAPDU = smartcardIO.runAPDU(c);
            }
        }
    }

    public static void main(String[] args) {
        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            smartcardIO.setup();
            testIsoApplet(smartcardIO);
            smartcardIO.teardown();
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        }
    }

}
