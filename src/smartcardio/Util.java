package smartcardio;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {
    public static String hex2(int hex) {
        return String.format("%02X", hex & 0xff);
    }

    public static void printHex(byte[] barr) {
        if (barr == null) {
            System.out.print("null");
        } else {
            for (byte b : barr) {
                System.out.print(" " + hex2(b));
            }
        }
    }

    public static void printProviders() {
        Provider providers[] = Security.getProviders();
        for (Provider provider : providers) {
            System.out.println(provider.toString());
            for (Object o : provider.entrySet()) {
                System.out.println(o);
            }
            System.out.println();
        }
    }

    public static void printAliases(KeyStore ks, char[] pin) {
        try {
            for (Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements();) {
                String alias = aliases.nextElement();
                System.out.println(alias);
                Certificate certificate = ks.getCertificate(alias);
                System.out.println(certificate);
                Key key = ks.getKey(alias, pin);
                System.out.println(key);
//            System.out.println(key.getFormat());
//            byte[] encoded = key.getEncoded();
//            printHex(encoded);
//            System.out.println();
            }
        } catch (KeyStoreException ex) {
            Logger.getLogger(SmartcardIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SmartcardIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(SmartcardIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
