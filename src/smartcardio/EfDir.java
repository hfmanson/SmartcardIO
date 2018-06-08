package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class EfDir {

    public static void efdir(SmartcardIO smartcardIO) throws CardException {

        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { (byte) 0x2f, (byte) 0x00 });
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            do {
                c = new CommandAPDU(0x00, 0xB2, 0x00, 0x02, 0x100);
            } while (smartcardIO.runAPDU(c).getSW() == SmartcardIO.SW_NO_ERROR);
        }
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
