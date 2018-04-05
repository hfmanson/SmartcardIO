package smartcardio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.AuthProvider;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.x500.X500Principal;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.X500Name;

public class GenerateCSR {

    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;
    private KeyPairGenerator keyGen = null;
    private static GenerateCSR gcsr = null;

    private GenerateCSR(final String configName, final char[] password) {

        try {
            if (configName == null) {
                keyGen = KeyPairGenerator.getInstance("RSA");
            } else {
                AuthProvider authProvider = new sun.security.pkcs11.SunPKCS11(configName);
                Security.addProvider(authProvider);
                authProvider.login(null, new CallbackHandler() {
                    @Override
                    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                        for (Callback callback : callbacks) {
                            if (callback instanceof PasswordCallback) {
                                PasswordCallback passwordCallback = (PasswordCallback) callback;
                                passwordCallback.setPassword(password);
                            }
                        }
                    }
                });
                keyGen = KeyPairGenerator.getInstance("RSA", authProvider);
            }
        } catch (NoSuchAlgorithmException | LoginException ex) {
            Logger.getLogger(GenerateCSR.class.getName()).log(Level.SEVERE, null, ex);
        }
        keyGen.initialize(2048, new SecureRandom());
        KeyPair keypair = keyGen.generateKeyPair();
        publicKey = keypair.getPublic();
        privateKey = keypair.getPrivate();
    }

    public static GenerateCSR getInstance(final String configName, final char[] password) {
        if (gcsr == null) {
            gcsr = new GenerateCSR(configName, password);
        }
        return gcsr;
    }

    /**
     *
     * @param name X500Principal name
     * @return
     * @throws Exception
     */
    public String getCSR(final String name) throws Exception {
        // generate PKCS10 certificate request
        final String sigAlg = "SHA256WithRSA";
        final PKCS10 pkcs10 = new PKCS10(publicKey);
        final Signature signature = Signature.getInstance(sigAlg);
        signature.initSign(privateKey);
        final X500Principal principal = new X500Principal(name);
        final X500Name x500name = new X500Name(principal.getEncoded());
        pkcs10.encodeAndSign(x500name, signature);
        byte[] c;
        try (ByteArrayOutputStream bs = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(bs)) {
            pkcs10.print(ps);
            c = bs.toByteArray();
        }
        String csr = new String(c);
        return csr;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 3) {
            System.err.println("Usage: java smartcardio.GenerateCSR <name>\n\tor java smartcardio.GenerateCSR <name> <PKCS11 config file> <keystore password>");
            return;
        }
        final String name = args[0];
        final String configName = args.length == 3 ? args[1] : null;
        final char password[] = args.length == 3 ? args[2].toCharArray() : null;
        final GenerateCSR mygcsr = GenerateCSR.getInstance(configName, password);
        System.out.println("Public Key:\n" + mygcsr.getPublicKey().toString());
        System.out.println("Private Key:\n" + mygcsr.getPrivateKey().toString());
        // common, orgUnit, org, locality, state, country
        String csr = mygcsr.getCSR(name);
        System.out.println("CSR Request Generated!!");
        System.out.println(csr);
    }
}
