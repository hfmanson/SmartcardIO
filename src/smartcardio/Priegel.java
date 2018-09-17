package smartcardio;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Priegel {
    public final static byte[] AID_ISOAPPLET = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };
    public final static byte[] AID_JOOSTAPPLET = { (byte) 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 };
    public final static byte[] AID_3GPP = { (byte) 0xA0, 0x00, 0x00, 0x00, (byte) 0x87 };
    public final static byte[] AID_M4M = { (byte) 0xA0, 0x00, 0x00, 0x03, (byte) 0x96, 0x4D, 0x34, 0x4D };

    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws java.lang.IllegalArgumentException if input length is incorrect
     */
    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public static void priegel2(SmartcardIO smartcardIO) throws CardException {

        // Send Select Applet command
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { 0x3f, 0x00, 0x2f, 0x00 });
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == 0x9000) {
            // Login with PIN "1234"
            smartcardIO.login(new byte[] { 0x31, 0x32, 0x33, 0x34 });

            // 00:22:41:00:06:80:01:F3:84:01:00
            //c = new CommandAPDU(0x00, 0x22, 0x41, 0x00, new byte[] { (byte) 0x80, 0x01, (byte) 0xF3, (byte) 0x84, 0x01, 0x00 });
            //smartcardIO.runAPDU(c);


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
    }

    public static void priegel(SmartcardIO smartcardIO) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_M4M);
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == 0x9000) {
            System.err.println("OK");
        }
    }

    public static void priegel3(SmartcardIO smartcardIO) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x00, 0x04, new byte[] { 0x2f, 0x00 });
        //CommandAPDU c = new CommandAPDU(0x00, 0x04, 0x00, 0x00, new byte[] { 0x2f, 0x00 });
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == 0x9000) {
            System.err.println("OK");
            c = new CommandAPDU(0x00, 0xB2, 0x01, 0x04, 0x100);
            smartcardIO.runAPDU(c);
        }
    }

    public static void priegel4(SmartcardIO smartcardIO) throws CardException {
        //0xFE 0x17 0xFE 0xFE 0x00 0x00
        CommandAPDU c = new CommandAPDU(0xFE, 0x17, 0xFE, 0xFE, 0x100);
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(c);
        if (responseAPDU.getSW() == 0x9000) {
            System.err.println("OK");
        }
    }

    public static void challengeTest(SmartcardIO smartcardIO) throws CardException {
        CommandAPDU commandAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_ISOAPPLET);
        ResponseAPDU responseAPDU = smartcardIO.runAPDU(commandAPDU);
        if (responseAPDU.getSW() == 0x9000) {
            System.err.println("OK 1");
            responseAPDU = smartcardIO.login(new byte[] { 0x31, 0x32, 0x33, 0x34 });
            if (responseAPDU.getSW() == 0x9000) {
                System.err.println("OK 2");
                commandAPDU = new CommandAPDU(0x00, 0x22, 0x41, 0xb6, new byte[]{(byte) 0x80, (byte) 0x01, (byte) 0x11, (byte) 0x81, (byte) 0x02, (byte) 0x50, (byte) 0x15, (byte) 0x84, (byte) 0x01, (byte) 0x00});
                responseAPDU = smartcardIO.runAPDU(commandAPDU);
                if (responseAPDU.getSW() == 0x9000) {
                    System.err.println("OK 3");
                    byte[] challenge = HexStringToByteArray("3051300D060960864801650304020305000440FC936E9CE8B5250339585207FE555300FA2428F8CCCD3A28C704ED3D332D6565BDF440427BBE4E0F2EA9ED3268CE537ABD56434D0B930BDF72064518CD8DD825");
                    commandAPDU = new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, challenge);
                    responseAPDU = smartcardIO.runAPDU(commandAPDU);
                    if (responseAPDU.getSW() == 0x9000) {
                        System.err.println("OK 4");
                        byte[] data = responseAPDU.getData();
                        if (data == null) {
                            System.err.println("data is null!");
                        } else {
                            System.out.println("signature: " + ByteArrayToHexString(data));
                        }
                    }
                }
            }
        }
    }



    public static void main(String[] args) {
        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            smartcardIO.setup();
            challengeTest(smartcardIO);
            smartcardIO.teardown();
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        }
    }

}
