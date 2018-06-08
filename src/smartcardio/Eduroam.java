package smartcardio;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Eduroam {
    public final static int OFFSET_USER = 0x00;
    public final static int OFFSET_PASSWORD = 0x20;
    public final static int FID_EDUROAM = 0x1000;
    public final static byte[] AID_ISOAPPLET = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };

    private final SmartcardIO smartcardIO;

    public Eduroam() throws CardException {
        smartcardIO = new SmartcardIO();
        smartcardIO.debug = true;
        smartcardIO.setup();
        smartcardIO.selectAID(AID_ISOAPPLET);
    }

    public void teardown() {
        smartcardIO.teardown();
    }

    public ResponseAPDU login(byte password[]) throws CardException {
        return smartcardIO.login(password);
    }

    public ResponseAPDU selectEduroam() throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x00, 0x00, new byte[] { 0x10, 0x00});
        return smartcardIO.runAPDU(c);
    }

    public ResponseAPDU createEduroam() throws CardException {
        return smartcardIO.createFile(FID_EDUROAM);
    }

    public ResponseAPDU readEduroam() throws CardException {
        ResponseAPDU responseAPDU = selectEduroam();
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            System.out.println("reading eduroam");
            responseAPDU = smartcardIO.readBinary();
        }
        return responseAPDU;
    }

    public boolean updateEduroam(byte[] data) throws CardException {
        boolean result = false;
        ResponseAPDU responseAPDU = selectEduroam();
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            System.out.println("updating eduroam");
            result = smartcardIO.updateBinary(data).getSW() == SmartcardIO.SW_NO_ERROR;
        }
        return result;
    }

    public static void copyStringToByteArray(byte[] barr, int offset, String s) {
        for (int i = 0; i < s.length(); i++) {
            barr[offset + i] = (byte) s.charAt(i);
        }
    }

    public static String readStringFromByteArray(byte[] barr, int offset) {
        String result = "";
        byte b;
        int i = offset;
        while ((b = barr[i++]) != (byte) 0xFF) {
            result += (char) b;
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            Eduroam eduroam = new Eduroam();
            byte data[];
            if (args.length >= 2) {
                data = new byte[64];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) 0xFF;
                }
                Eduroam.copyStringToByteArray(data, OFFSET_USER, args[0]);
                Eduroam.copyStringToByteArray(data, OFFSET_PASSWORD, args[1]);
                ResponseAPDU responseAPDU = null;
                if (args.length == 3) {
                    eduroam.login(args[2].getBytes());
                }
                responseAPDU = eduroam.selectEduroam();
                if (responseAPDU.getSW() != SmartcardIO.SW_NO_ERROR) {
                    System.out.println("FID 0x" + String.format("%04X", FID_EDUROAM) + " not present, creating...");
                    responseAPDU = eduroam.createEduroam();
                    if (responseAPDU.getSW() != SmartcardIO.SW_NO_ERROR) {
                        System.err.println("Error creating FID 0x" + String.format("%04X", FID_EDUROAM));
                        return;
                    }
                }
                if (!eduroam.updateEduroam(data)) {
                    System.err.println("Error updating eduroam data");
                    return;
                }
            } else if (args.length == 1) {
                eduroam.login(args[0].getBytes());
            }
            ResponseAPDU responseAPDU = eduroam.readEduroam();
            if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
                data = responseAPDU.getData();
                String user = Eduroam.readStringFromByteArray(data, OFFSET_USER);
                String password = Eduroam.readStringFromByteArray(data, OFFSET_PASSWORD);
                System.out.println("user: " + user);
                System.out.println("password: " + password);
            } else {
                System.err.println("No credentials found");
            }
        } catch (CardException ex) {
            Logger.getLogger(Eduroam.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
