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
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class SimSecureRandom extends SecureRandomSpi {
    public final static byte[] AID_ISOAPPLET = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };
    private CardChannel channel;
    private SmartcardIO smartcardIO;


    public SimSecureRandom() throws CardException {
        smartcardIO = new SmartcardIO();
        smartcardIO.debug = true;
        smartcardIO.setup();
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
            CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_ISOAPPLET);
            ResponseAPDU response = smartcardIO.runAPDU(c);
            if (response.getSW() == SmartcardIO.SW_NO_ERROR) {
                // Send test command
                c = new CommandAPDU(0x00, 0x84, 0x00, 0x00, numBytes);
                response = smartcardIO.runAPDU(c);
                if (response.getSW() == SmartcardIO.SW_NO_ERROR) {
                    data = response.getData();
                }

            }
        } catch (CardException ex) {
            Logger.getLogger(SimSecureRandom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
}
