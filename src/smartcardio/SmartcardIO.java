package smartcardio;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class SmartcardIO {
    public boolean debug = false;
    private Card card;
    private CardChannel cardChannel;

    public byte[] runAPDU(CommandAPDU c) throws CardException {
        if (debug) {
            System.out.print("command: CLA: " + Util.hex2(c.getCLA()) + ", INS: " + Util.hex2(c.getINS()) + ", P1: " + Util.hex2(c.getP1()) + ", P2: " + Util.hex2(c.getP2()));
            int nc = c.getNc();
            if (nc > 0) {
                System.out.print(", Nc: " + Util.hex2(nc) + ", data:");
                Util.printHex(c.getData());
            }
            System.out.println(", Ne: " + Util.hex2(c.getNe()));
        }
        byte[] data = null;
        ResponseAPDU answer = cardChannel.transmit(c);
        int status = answer.getSW();
        if (status == 0x9000) {
            data = answer.getData();
            if (debug) {
                System.out.print("answer: " + answer.toString() + ", data:");
                Util.printHex(data);
                System.out.println();
            }
        } else {
            System.out.println("ERROR: status: " + String.format("%04X", status));
        }
        return data;
    }

    public byte[] login(byte[] password) throws CardException {
        return runAPDU(new CommandAPDU(0x00, 0x20, 0x00, 0x01, password));
    }

    public byte[] generaterandom() throws CardException {
        return runAPDU(new CommandAPDU(0x00, 0x84, 0x00, 0x00, 0x10));
    }

    public void setup() throws CardException {
        // Display the list of terminals
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        if (debug) {
            System.out.println("Terminals: " + terminals);
        }

        // Use the first terminal
        CardTerminal terminal = terminals.get(0);

        // Connect wit hthe card
        card = terminal.connect("*");
        if (debug) {
            System.out.println("card: " + card);
        }
        cardChannel = card.getBasicChannel();
    }

    public void teardown() {
        try {
            // Disconnect the card
            card.disconnect(false);
        } catch (CardException ex) {
            Logger.getLogger(SmartcardIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Card getCard() {
        return card;
    }

    public byte[] readRecord(int record) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xB2, record, 0x04, 0x100);
        return runAPDU(c);
    }

    public void readRecords() throws CardException {
        int record = 1;
        while (readRecord(record++) != null) {
        }
    }

    public void updateRecord(int record, byte[] data) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xdc, record, 0x04, data);
        runAPDU(c);
    }

    public static byte hi(int x) {
        return (byte) (x >> 8);
    }

    public static byte lo(int x) {
        return (byte) (x & 0xff);
    }

    public byte[] selectTelecom(int fid) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x08, 0x0c, new byte[] { 0x7f, 0x10, hi(fid), lo(fid) });
        return runAPDU(c);
    }

    public byte[] readTelecomRecord(int fid, int record) throws CardException {
        byte result[] = null;
        if (selectTelecom(fid) != null) {
            System.out.println(String.format("reading telecom %04X", fid));
            result = readRecord(record);
        }
        return result;
    }

    public void readTelecomRecords(int fid) throws CardException {
        if (selectTelecom(fid) != null) {
            System.out.println(String.format("reading telecom %04X", fid));
            readRecords();
        }
    }

    public void writeTelecom(int fid, int record, byte[] data) throws CardException {
        if (selectTelecom(fid) != null) {
            System.out.println(String.format("write telecom %04X, record %d", fid, record));
            updateRecord(record, data);
        }
    }
}
