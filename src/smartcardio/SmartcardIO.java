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
    private CardTerminal terminal;
    private Card card;
    private CardChannel cardChannel;
    private static SmartcardIO smartcardIO;
    private static final int MAX_PAYLOAD_SIZE = 0xF0;
    private static final int CLA_CHAINING_MASK = 0x10;

    public static SmartcardIO getInstance() throws CardException {
        if (smartcardIO == null) {
            try {
                smartcardIO = new SmartcardIO();
                smartcardIO.debug = true;
                String reader = System.getProperty("smartcardio.reader");
                if (reader == null) {
                    smartcardIO.setup();
                } else {
                    smartcardIO.setup(reader);
                }
            } catch (CardException ex) {
                Logger.getLogger(SmartcardIO.class.getName()).log(Level.SEVERE, null, ex);
                smartcardIO = null;
            }
        }
        return smartcardIO;
    }

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
    public ResponseAPDU runAPDU1(CommandAPDU command) throws CardException {
        int payloadSize = command.getNc();
        int offset = 0;
        int cla = command.getCLA();
        int ins = command.getINS();
        int p1 = command.getP1();
        int p2 = command.getP2();
        byte[] data = command.getData();
        int ne = command.getNe();
        while (payloadSize > MAX_PAYLOAD_SIZE) {
            CommandAPDU chainCommand = new CommandAPDU(cla | CLA_CHAINING_MASK, ins, p1, p2, data, offset, MAX_PAYLOAD_SIZE, ne);
            runAPDU(chainCommand);
            offset += MAX_PAYLOAD_SIZE;
            payloadSize -= MAX_PAYLOAD_SIZE;
        }
        CommandAPDU finalCommand = new CommandAPDU(cla, ins, p1, p2, data, offset, payloadSize, ne);
        return runAPDU(finalCommand);
    }

    public ResponseAPDU login(byte[] password) throws CardException {
        return runAPDU(new CommandAPDU(0x00, 0x20, 0x00, 0x01, password));
    }

    public ResponseAPDU generaterandom() throws CardException {
        return runAPDU(new CommandAPDU(0x00, 0x84, 0x00, 0x00, 0x10));
    }

    public List<CardTerminal> listTerminals() throws CardException {
        // Display the list of terminals
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        if (debug) {
            System.out.println("Terminals: " + terminals);
        }
        return terminals;
    }

    public void setup(CardTerminal terminal) throws CardException {
        this.terminal = terminal;
        //System.out.println(terminal.getName());
        //if (debug) {
            System.out.println("Waiting for card presence...");
        //}
        terminal.waitForCardPresent(0);
        // Connect wit the card
        card = terminal.connect("*");
        if (debug) {
            System.out.print("card: " + card + ", ATR: ");
            Util.printHex(card.getATR().getBytes());
            //System.out.print(", Historical: ");
            //Util.printHex(card.getATR().getHistoricalBytes());
            System.out.println();
        }
        cardChannel = card.getBasicChannel();
    }

    public void waitForCardPresent() {
        try {
            terminal.waitForCardPresent(0);
        } catch (CardException ex) {
        }
    }

    public void waitForCardAbsent() {
        try {
            terminal.waitForCardAbsent(0);
        } catch (CardException ex) {
        }
    }

    public void setup(int terminalNumber) throws CardException {
        List<CardTerminal> terminals = listTerminals();
        if (terminals.size() > terminalNumber) {
            CardTerminal terminal = terminals.get(terminalNumber);
            setup(terminal);
        } else {
            System.err.println("No terminal with number " + terminalNumber);
            System.exit(1);
        }
    }

    public void setup(String terminalName) throws CardException {
        TerminalFactory factory = TerminalFactory.getDefault();
        factory.terminals().getTerminal(terminalName);
        CardTerminal terminal = factory.terminals().getTerminal(terminalName);
        setup(terminal);
    }

    public void setup() throws CardException {
        setup(0);
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
        ResponseAPDU responseAPDU;
        do {
            responseAPDU = readRecord(record++);
        } while (responseAPDU != null && responseAPDU.getSW() == 0x9000);
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
