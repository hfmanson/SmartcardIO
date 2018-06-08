package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Sysmocom {
    public final static byte[] AID_ISOAPPLET = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };
    public final static byte[] AID_HELLOAPPLET = { (byte) 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01 };

    public static void sdaid(SmartcardIO smartcardIO) throws CardException {

        //CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, 0x100);
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] { (byte) 0xA0 , 0x00, 0x00, 0x01, 0x51 });

        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {

        }
        //if (smartcardIO.runAPDU(c) != null) {
            //c = new CommandAPDU(0x80, 0xF2, 0x00, 0x01, 0x100);
            //smartcardIO.runAPDU(c);
        //}
    }

    public static void efdir(SmartcardIO smartcardIO) throws CardException {

        //CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] { (byte) 0xFF, 0x00, 0x00, 0x01, 0x51, 0x00, 0x00, 0x01 });
        //CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, 0x100);
        CommandAPDU c = new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, new byte[] { (byte) 0x2f, (byte) 0x00 });
        //CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] { (byte) 0xA0 , 0x00, 0x00, 0x01, 0x51 });
//        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[] {
//            (byte) 0xA0,
//            (byte) 0x00,
//            (byte) 0x00,
//            (byte) 0x00,
//            (byte) 0x87,
//            (byte) 0x10,
//            (byte) 0x02,
//            (byte) 0xFF,
//            (byte) 0x49,
//            (byte) 0xFF,
//            (byte) 0xFF,
//            (byte) 0x89,
//            (byte) 0x04,
//            (byte) 0x0B,
//            (byte) 0x00,
//            (byte) 0x00
//        });
        //A0 00 00 01 510000
        //A0 00 00 00 03000000
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW1() == 0x9f) {
            CommandAPDU c2 = new CommandAPDU(c.getCLA(), 0xC0, 0x00, 0x00, (byte) responseAPDU.getSW2());
            responseAPDU = smartcardIO.runAPDU(c2);
        }
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            do {
                byte[] data = responseAPDU.getData();
                c = new CommandAPDU(0xA0, 0xB2, 0x00, 0x02, data[14]);
            } while (smartcardIO.runAPDU(c).getSW() == SmartcardIO.SW_NO_ERROR);

        }
        //if (smartcardIO.runAPDU(c) != null) {
            //c = new CommandAPDU(0x80, 0xF2, 0x00, 0x01, 0x100);
            //smartcardIO.runAPDU(c);
        //}
    }

    public static void main(String[] args) {
        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            smartcardIO.setup();
            efdir(smartcardIO);
            smartcardIO.teardown();
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        }
    }
}
