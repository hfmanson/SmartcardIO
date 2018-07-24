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
    public static final int SW_NO_ERROR = 0x9000;
    private Card card;
    private CardChannel cardChannel;

    public static byte hi(int x) {
        return (byte) (x >> 8);
    }

    public static byte lo(int x) {
        return (byte) (x & 0xff);
    }

    public ResponseAPDU runAPDU(CommandAPDU c) throws CardException {
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
        return answer;
    }

    public ResponseAPDU login(byte[] password) throws CardException {
        return runAPDU(new CommandAPDU(0x00, 0x20, 0x00, 0x01, password));
    }

    public ResponseAPDU generaterandom() throws CardException {
        return runAPDU(new CommandAPDU(0x00, 0x84, 0x00, 0x00, 0x10));
    }

    public void setup() throws CardException {
        // Display the list of terminals
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        if (debug) {
            System.out.println("Terminals: " + terminals);
        }
        if (terminals.size() > 0) {
            // Use the first terminal
            CardTerminal terminal = terminals.get(0);
            if (debug) {
                System.out.println("Waiting for card presence...");
            }
            terminal.waitForCardPresent(0);
            // Connect wit hthe card
            card = terminal.connect("*");
            if (debug) {
                System.out.print("card: " + card + ", ATR: ");
                Util.printHex(card.getATR().getBytes());
                //System.out.print(", Historical: ");
                //Util.printHex(card.getATR().getHistoricalBytes());
                System.out.println();
            }
            cardChannel = card.getBasicChannel();
        } else {
            System.err.println("No terminals");
            System.exit(1);
        }
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

    public ResponseAPDU selectAID(byte aid[]) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid);
        return runAPDU(c);
    }

    public ResponseAPDU readBinary() throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xB0, 0x00, 0x00, 0x100);
        return runAPDU(c);
    }

    public ResponseAPDU updateBinary(byte data[]) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xD6, 0x00, 0x00, data);
        return runAPDU(c);
    }

    public ResponseAPDU readRecord(int record) throws CardException {
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

    public ResponseAPDU createFile(int fid) throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xE0, 0x00, 0x00, new byte[] {
            0x6f,
            0x15,
                (byte) 0x81,
                0x02,
                    0x00, 0x40,
                (byte) 0x82,
                0x01,
                    0x01,
                (byte) 0x83,
                0x02,
                    hi(fid), lo(fid),
                (byte) 0x86,
                0x08,
                    (byte) 0xFF, (byte) 0x90, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x90, (byte) 0x90
        });
        return runAPDU(c);
    }
}
