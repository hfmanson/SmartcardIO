package smartcardio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.xml.bind.DatatypeConverter;

public class LoyalityCard {
    public final static byte[] AID_LOYALTY_CARD_AID = { (byte) 0xF2, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22 };

    public static void readCard(SmartcardIO smartcardIO, byte[] digest) throws CardException {
        CommandAPDU c;
        c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_LOYALTY_CARD_AID);
        ResponseAPDU rapdu = smartcardIO.runAPDU(c);
        if (rapdu.getSW() == 0x9000) {
            c = new CommandAPDU(0x00, 0x55, 0x00, 0x00, digest);
            rapdu = smartcardIO.runAPDU(c);
        }
    }

    public static void main(String[] args) {
        try {
            FileInputStream is = new FileInputStream(args[0]);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(is);
            byte[] digest = getThumbprint(cert);
            String digestHex = DatatypeConverter.printHexBinary(digest);
            System.out.println(digestHex);
            
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            smartcardIO.setup();
            readCard(smartcardIO, digest);
            smartcardIO.teardown();
        } catch (Exception e) {
            System.err.println("Ouch: " + e.toString());
        }
    }
    
    public static byte[] getThumbprint(X509Certificate cert)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = cert.getEncoded();
        md.update(der);
        return md.digest();        
    }
}
