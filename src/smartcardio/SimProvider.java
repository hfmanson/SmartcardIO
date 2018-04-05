package smartcardio;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimProvider extends Provider {
    public SimProvider() {
        super("SimProvider", 1.0, "SIM provider");
        put("SecureRandom.SIM-PRNG", "smartcardio.SimSecureRandom");
        put("KeyStore.SIM", "smartcardio.SimKeystore");
        put("Signature.SHA256withRSA", "smartcardio.SimSignature");
        put("Signature.SHA256withRSA SupportedKeyClasses", "smartcardio.SimPrivateKey");
        SimService s = new SimService(this, "SIM", "RSA", SimService.class.getName(), null, null);
        putService(s);

    }

    private static final class SimService extends Service {

        public SimService(Provider provider, String type, String algorithm, String className, List<String> aliases, Map<String, String> attributes) {
            super(provider, type, algorithm, className, aliases, attributes);
        }

        @Override
        public boolean supportsParameter(Object param) {
            return param instanceof SimPrivateKey;
        }
    }

    public static void printRandom(byte random[]) {
        Util.printHex(random);
        System.out.println();
    }

    public static void testSecureRandom() {
        try {
            Provider p = new SimProvider();
            Security.addProvider(p);
            SecureRandom secureRandom = SecureRandom.getInstance("SIM-PRNG");
            byte[] random = secureRandom.generateSeed(64);
            printRandom(random);
            secureRandom.nextBytes(random);
            printRandom(random);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void testKeyStore() {
        try {
            Provider p = new SimProvider();
            Security.addProvider(p);
            //Util.printProviders();
            KeyStore ks = KeyStore.getInstance("SIM");
            ks.load(null, new char[] { '1', '2', '3', '4' });
            System.out.println(ks);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        //testKeyStore();
        testSecureRandom();
    }

}
