package smartcardio;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Telecom {
    public final static int EXT_RECORD_SIZE = 13;
    public final static int EF_EXT1 = 0x6F4A;
    public final static int EF_EXT2 = 0x6F4B;
    public final static int RECORD_USER = 1;
    public final static int RECORD_PASSWORD = 3;
    private final SmartcardIO smartcardIO;

    public Telecom() throws CardException {
        smartcardIO = new SmartcardIO();
        smartcardIO.debug = true;
        smartcardIO.setup();
        smartcardIO.login(new byte[] { 0x31, 0x32, 0x33, 0x34, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
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

    public ResponseAPDU selectTelecom(int fid) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x08, 0x0c, new byte[] { 0x7f, 0x10, SmartcardIO.hi(fid), SmartcardIO.lo(fid) });
        return smartcardIO.runAPDU(c);
    }

    public ResponseAPDU readTelecomRecord(int fid, int record) throws CardException {
        ResponseAPDU responseAPDU = selectTelecom(fid);
        if (responseAPDU.getSW() == SmartcardIO.SW_NO_ERROR) {
            System.out.println(String.format("reading telecom %04X", fid));
            responseAPDU = smartcardIO.readRecord(record);
        }
        return responseAPDU;
    }

    public void readTelecomRecords(int fid) throws CardException {
        if (selectTelecom(fid) != null) {
            System.out.println(String.format("reading telecom %04X", fid));
            smartcardIO.readRecords();
        }
    }

    public void writeTelecom(int fid, int record, byte[] data) throws CardException {
        if (selectTelecom(fid) != null) {
            System.out.println(String.format("write telecom %04X, record %d", fid, record));
            smartcardIO.updateRecord(record, data);
        }
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
            writeTelecom(ext, recordnr++, createExtRecord(data.substring(beginIndex, endIndex)) );
            beginIndex = endIndex;
        }
    }

    public String readData(int ext, int recordnr) throws CardException {
        String result = "";
        ResponseAPDU responseAPDU;
        while ((responseAPDU = readTelecomRecord(ext, recordnr++)).getSW() == SmartcardIO.SW_NO_ERROR) {
            byte record[] = responseAPDU.getData();
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
        try {
            Telecom telecom = new Telecom();
            if (args.length == 2) {
                telecom.writeEduroamUser(args[0]);
                telecom.writeEduroamPassword(args[1]);
            }
            telecom.readTelecomRecords(EF_EXT1);
            System.err.println(telecom.readEduroamUser());
            System.err.println(telecom.readEduroamPassword());
            telecom.teardown();
        } catch (CardException ex) {
            Logger.getLogger(Telecom.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
