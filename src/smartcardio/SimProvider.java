package smartcardio;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
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
        //SimService s = new SimService(this, "SIM", "RSA", SimService.class.getName(), null, null);
        //putService(s);

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
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                System.out.println("alias: " + alias);
            }
            System.out.println(ks.containsAlias("Certificate"));
            System.out.println(ks.containsAlias("larie"));
            System.out.println(ks.size());
            Certificate certificate = ks.getCertificate("Certificate");
            System.out.println(certificate.getPublicKey());
            RSAPrivateKey privatekey = (RSAPrivateKey) ks.getKey("Private Key", null);
            System.out.println(privatekey.toString());
            Signature s = Signature.getInstance("SHA256withRSA", p);
            s.initSign(privatekey);
            byte buf[] = new byte[] { 0x48, 0x45, 0x4e, 0x52, 0x49 };
            s.update(buf);
            s.sign();
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(SimProvider.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        testKeyStore();
        //testSecureRandom();
    }

}
