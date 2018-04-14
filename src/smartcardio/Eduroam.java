package smartcardio;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;

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

    public byte[] login(byte password[]) throws CardException {
        return smartcardIO.login(password);
    }

    public byte[] selectEduroam() throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x00, 0x00, new byte[] { 0x10, 0x00});
        return smartcardIO.runAPDU(c);
    }

    public byte[] createEduroam() throws CardException {
        return smartcardIO.createFile(FID_EDUROAM);
    }

    public byte[] readEduroam() throws CardException {
        byte result[] = null;
        if (selectEduroam() != null) {
            System.out.println("reading eduroam");
            result = smartcardIO.readBinary();
        }
        return result;
    }

    public boolean updateEduroam(byte[] data) throws CardException {
        boolean result = false;
        if (selectEduroam() != null) {
            System.out.println("updating eduroam");
            result = smartcardIO.updateBinary(data) != null;
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
                if (args.length == 3) {
                    eduroam.login(args[2].getBytes());
                }
                if (eduroam.selectEduroam() == null) {
                    System.out.println("FID 0x" + String.format("%04X", FID_EDUROAM) + " not present, creating...");
                    eduroam.createEduroam();
                }
                if (!eduroam.updateEduroam(data)) {
                    System.err.println("Error updating eduroam data");
                    return;
                }
            }
            data = eduroam.readEduroam();
            if (data != null) {
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
