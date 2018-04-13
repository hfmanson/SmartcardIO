package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;

public class Joost {
    public final static byte[] AID_ISOAPPLET = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };
    public final static byte[] AID_HELLOAPPLET = { (byte) 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01 };
    public final static byte[] AID_JOOSTAPPLET = { (byte) 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 };

    public static void joost(SmartcardIO smartcardIO) throws CardException {

        // Send Select Applet command
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_ISOAPPLET);
        smartcardIO.runAPDU(c);

        // Login with PIN "1234"
        smartcardIO.login(new byte[] { 0x31, 0x32, 0x33, 0x34 });

        // 00:22:41:00:06:80:01:F3:84:01:00
        c = new CommandAPDU(0x00, 0x22, 0x41, 0x00, new byte[] { (byte) 0x80, 0x01, (byte) 0xF3, (byte) 0x84, 0x01, 0x00 });
        smartcardIO.runAPDU(c);


        //3f0050153201
        //c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { 0x50, 0x15, 0x44, 0x02 });
        c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { 0x50, 0x15, 0x50, 0x32 });
        //c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { 0x50, 0x15, 0x32, 0x01 });

//            c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { 0x3f, 0x00, 0x2f, 0x00 });
        if (smartcardIO.runAPDU(c) != null) {
            // 00:B0:00:00:00
            c = new CommandAPDU(0x00, 0xB0, 0x00, 0x00, 0x100);
            smartcardIO.runAPDU(c);
            //c = new CommandAPDU(0x00, 0xA4, 0x03, 0x00, 0x100);
            //smartcardIO.runAPDU(c);
        }
    }


    public static void main(String[] args) {
        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            smartcardIO.setup();
            joost(smartcardIO);
            smartcardIO.teardown();
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        }
    }
}
