package smartcardio;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;

public class Eduroam {
    public final static int EXT_RECORD_SIZE = 13;
    public final static int EF_EXT1 = 0x6F4A;
    public final static int EF_EXT2 = 0x6F4B;
    public final static int RECORD_USER = 1;
    public final static int RECORD_PASSWORD = 3;

    private final SmartcardIO smartcardIO;

    public Eduroam() throws CardException {
        smartcardIO = new SmartcardIO();
        smartcardIO.debug = true;
        smartcardIO.setup();
    }

    public void teardown() {
        smartcardIO.teardown();
    }
    // convert data to bytes 0,(data bytes) padded with 0xFF
    public static byte[] createExtRecord(String data) {
        byte result[] = new byte[EXT_RECORD_SIZE];
        result[0] = 0;
        byte barr[] = data.getBytes();
        int length = barr.length;
        if (length > EXT_RECORD_SIZE - 1) {
            length = EXT_RECORD_SIZE - 1;
        }
        System.arraycopy(barr, 0, result, 1, length);
        for (int i = length + 1; i < EXT_RECORD_SIZE; i++) {
            result[i] = (byte) 0xFF;
        }
        return result;
    }

    public void writeData(int ext, int recordnr, String data) throws CardException {
        int beginIndex = 0;
        int length;
        while ((length = data.length() - beginIndex) > 0)
        {
            if (length > EXT_RECORD_SIZE - 1) {
                length = EXT_RECORD_SIZE - 1;
            }
            int endIndex = beginIndex + length;
            smartcardIO.writeTelecom(ext, recordnr++, createExtRecord(data.substring(beginIndex, endIndex)) );
            beginIndex = endIndex;
        }
    }

    public String readData(int ext, int recordnr) throws CardException {
        String result = "";
        byte record[];
        while ((record = smartcardIO.readTelecomRecord(ext, recordnr++)) != null) {
            for (int i = 1; i < EXT_RECORD_SIZE; i++) {
                int val = (record[i] & 0xFF);
                if (val == 0xFF) {
                    return result;
                }
                result += String.valueOf((char) val);
            }
        }
        return result;
    }

    public void writeEduroamUser(String user) throws CardException {
        writeData(EF_EXT1, RECORD_USER, user);
    }

    public void writeEduroamPassword(String password) throws CardException {
        writeData(EF_EXT1, RECORD_PASSWORD, password);
    }

    public String readEduroamUser() throws CardException {
        return readData(EF_EXT1, RECORD_USER);
    }

    public String readEduroamPassword() throws CardException {
        return readData(EF_EXT1, RECORD_PASSWORD);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            try {
                //System.err.println("Usage: " + Eduroam.class.getName() + " <user> <password>");
                SmartcardIO smartcardIO = new SmartcardIO();
                smartcardIO.setup();
                smartcardIO.debug = true;
                smartcardIO.readTelecomRecords(EF_EXT1);
            } catch (CardException ex) {
                Logger.getLogger(Eduroam.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                Eduroam eduroam = new Eduroam();
                eduroam.writeEduroamUser(args[0]);
                System.err.println(eduroam.readEduroamUser());
                eduroam.writeEduroamPassword(args[1]);
                System.err.println(eduroam.readEduroamPassword());
                eduroam.teardown();
            } catch (Exception e) {
                System.err.println("Ouch: " + e.toString());
            }
        }
    }
}
