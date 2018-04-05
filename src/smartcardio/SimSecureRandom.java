/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartcardio;

import java.security.SecureRandomSpi;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.TerminalFactory;
import static smartcardio.SmartcardIO.aid;
import static smartcardio.SmartcardIO.runAPDU;

public class SimSecureRandom extends SecureRandomSpi {
    private CardChannel channel;

    public SimSecureRandom() {
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
            channel = card.getBasicChannel();
        } catch (CardException ex) {
            Logger.getLogger(SimSecureRandom.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    @Override
    protected void engineSetSeed(byte[] seed) {
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        int length = bytes.length;
        byte data[] = engineGenerateSeed(length);
        System.arraycopy(data, 0, bytes, 0, length);
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] data = null;
        try {
            // Send Select Applet command
            CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid);
            runAPDU(channel, c);

            // Send test command
            c = new CommandAPDU(0x00, 0x84, 0x00, 0x00, numBytes);
            data = runAPDU(channel, c);
        } catch (CardException ex) {
            Logger.getLogger(SimSecureRandom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
}
