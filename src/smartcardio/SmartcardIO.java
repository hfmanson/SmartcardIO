package smartcardio;

import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class SmartcardIO {

    public static byte[] aid = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };
    public static boolean DEBUG = false;

    public static byte[] runAPDU(CardChannel channel, CommandAPDU c) throws CardException {
        if (DEBUG) {
            System.out.print("command: CLA: " + Util.hex2(c.getCLA()) + ", INS: " + Util.hex2(c.getINS()) + ", P1: " + Util.hex2(c.getP1()) + ", P2: " + Util.hex2(c.getP2()));
            int nc = c.getNc();
            if (nc > 0) {
                System.out.print(", Nc: " + Util.hex2(nc) + ", data:");
                Util.printHex(c.getData());
            }
            System.out.println(", Ne: " + Util.hex2(c.getNe()));
        }
        ResponseAPDU answer = channel.transmit(c);
        byte[] data = answer.getData();
        if (DEBUG) {
            System.out.print("answer: " + answer.toString() + ", data:");
            Util.printHex(data);
            System.out.println();
        }
        return data;
    }

    public static void joost() {
        try {
            // Display the list of terminals
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals: " + terminals);

            // Use the first terminal
            CardTerminal terminal = terminals.get(0);

            // Connect wit hthe card
            Card card = terminal.connect("*");
            System.out.println("card: " + card);
            CardChannel channel = card.getBasicChannel();

            // Send Select Applet command
            //byte[] aid = {(byte)0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01};
            //byte[] aid = {(byte) 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01};
            CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid);
            runAPDU(channel, c);

            // Send test command
            c = new CommandAPDU(0x00, 0x84, 0x00, 0x00, 0x10);
            runAPDU(channel, c);
            // Login with PIN "1234"
            //c = new CommandAPDU(0x00, 0x20, 0x00, 0x01, new byte[] { 0x31, 0x32, 0x33, 0x34 });
            //runAPDU(channel, c);

            // 00:22:41:00:06:80:01:F3:84:01:00
            //c = new CommandAPDU(0x00, 0x22, 0x41, 0x00, new byte[] { (byte) 0x80, 0x01, (byte) 0xF3, (byte) 0x84, 0x01, 0x00 });
            //runAPDU(channel, c);


            // 00:A4:08:00:04:50:15:44:03
            //c = new CommandAPDU(0x00, 0xA4, 0x08, 0x00, new byte[] { 0x50, 0x15, 0x44, 0x02 });
            //runAPDU(channel, c);

            // 00:B0:00:00:00
            //c = new CommandAPDU(0x00, 0xB0, 0x00, 0x00, 0x100);
            //runAPDU(channel, c);


            // Disconnect the card
            card.disconnect(false);
        } catch (Exception e) {
            System.out.println("Ouch: " + e.toString());
        }
    }

    public static void main(String[] args) {
        joost();
    }
}
