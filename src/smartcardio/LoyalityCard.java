package smartcardio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import static keystore.KeystoreTest.PASSWORD;

public class LoyalityCard {
    public final static byte[] AID_LOYALTY_CARD_AID = { (byte) 0xF2, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22 };
    public final static int FINGERPRINT_LENGTH = 20;
    public static ResponseAPDU readCard(SmartcardIO smartcardIO, byte[] digest) throws CardException {
        CommandAPDU c;
        c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_LOYALTY_CARD_AID);
        ResponseAPDU rapdu = smartcardIO.runAPDU(c);
        if (rapdu.getSW() == 0x9000) {
            System.out.println("account: " + Util.ByteArrayToHexString(rapdu.getData()));
            c = new CommandAPDU(0x00, 0x55, 0x00, 0x00, digest);
            rapdu = smartcardIO.runAPDU(c);
        }
        return rapdu;
    }

    public static byte[] sign(SmartcardIO smartcardIO, byte[] challenge, byte[] slotSignature) throws CardException {
        byte[] signature = null;
        int payloadLength = challenge.length + slotSignature.length;
        byte[] payload = new byte[payloadLength];
        System.arraycopy(challenge, 0, payload, 0, challenge.length);
        System.arraycopy(slotSignature, 0, payload, challenge.length, slotSignature.length);
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_LOYALTY_CARD_AID);
        ResponseAPDU rapdu = smartcardIO.runAPDU(c);
        if (rapdu.getSW() == 0x9000) {
            System.out.println("payload length: " + payloadLength);
            c = new CommandAPDU(0x00, 0x56, 0x00, 0x00, payload, 0x80);
            rapdu = smartcardIO.runAPDU1(c);
            signature = rapdu.getData();
        }
        return signature;
    }
    public static boolean isRaspberry() {
        return System.getProperty("os.arch", "").equals("arm");
    }

    public static GpioPinDigitalOutput setupGpio() {
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn off
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.LOW);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);
        return pin;
    }

    public static X509Certificate CertificateFromFileName(String fileName) throws FileNotFoundException, CertificateException, IOException {
        X509Certificate result;
        try (FileInputStream is = new FileInputStream(fileName)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            result = (X509Certificate) certificateFactory.generateCertificate(is);
        }
        return result;
    }

    public static byte[] signChallenge(PrivateKey key, byte[] challenge) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        sig.update(challenge);
        return sig.sign();
    }

    public static KeyStore getKeyStore(String keyStoreFile, char[] password) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        File f = new File(keyStoreFile);
        try (InputStream is = new FileInputStream(f)) {
            ks.load(is, password);
        }
        return ks;
    }

    private static HashMap<Fingerprint, X509Certificate> certificateMap;

    private static void createCertificateMap(KeyStore ks) throws Exception {
        certificateMap = new HashMap<>();
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            System.out.println("alias: " + alias);
            System.out.println("********************************************************");
            Certificate certificate = ks.getCertificate(alias);
            X509Certificate x509 = (X509Certificate) certificate;
            byte[] fingerprint = getThumbprint(x509);
            certificateMap.put(new Fingerprint(fingerprint), x509);
            System.out.println("fingerprint:  " + Util.ByteArrayToHexString(fingerprint));
            System.out.println("********************************************************");
        }
    }

    public static void main(String[] args) {
        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            if (args.length == 0) {
                List<CardTerminal> terminals = smartcardIO.listTerminals();
                System.out.println("Terminals: " + terminals);
                System.exit(0);
            } else if (args.length < 2) {
                System.err.println("Usage: java smartcardio.LoyalityCard <keystore filename> <slot alias> [terminal name]\n\te.g. java smartcardio.LoyalityCard slot1.p12 slot1");
                System.exit(1);
            }

            KeyStore ks = getKeyStore(args[0], PASSWORD);
            createCertificateMap(ks);
            X509Certificate slot = (X509Certificate) ks.getCertificate(args[1]);

            byte[] slotFingerprint = getThumbprint(slot);
            System.out.println("slot sig alg: " + slot.getSigAlgName());
            System.out.println("slot fingerprint: " + Util.ByteArrayToHexString(slotFingerprint));
            GpioPinDigitalOutput pin = null;
            if (isRaspberry()) {
                pin = setupGpio();
            }
            while (true) {
                try {
                    if (args.length > 2) {
                        String terminalName = args[2];
                        smartcardIO.setup(terminalName);
                    } else {
                        smartcardIO.setup();
                    }
                    ResponseAPDU rapdu = readCard(smartcardIO, slotFingerprint);
                    if (rapdu.getSW() == 0x9000) {
                        byte[] encrypteddata = rapdu.getData();
                        System.out.println("received encrypted data: " + Util.ByteArrayToHexString(encrypteddata));
                        byte[] data = encrypteddata;
                        byte[] receivedKeyFingerprint = Arrays.copyOfRange(data, 0, FINGERPRINT_LENGTH);
                        byte[] random = Arrays.copyOfRange(data, FINGERPRINT_LENGTH, data.length);
                        System.out.println("received random bytes: " + Util.ByteArrayToHexString(random));

                        byte[] slotSignature = signChallenge((PrivateKey) ks.getKey(args[1], PASSWORD), random);
                        System.out.println("slot signature: " + Util.ByteArrayToHexString(slotSignature));
                        System.out.println("received key fingerprint: " + Util.ByteArrayToHexString(receivedKeyFingerprint));
                        X509Certificate keycert = certificateMap.get(new Fingerprint(receivedKeyFingerprint));
                        if (keycert == null) {
                            System.out.println("Key not found in keystore");
                        } else {
                            System.out.println("key alias: " + ks.getCertificateAlias(keycert));
                            System.out.println("key issuer: " + keycert.getIssuerDN().getName());
                            System.out.println("key subject: " + keycert.getSubjectDN().getName());
                            SecureRandom secureRandom = new SecureRandom();
                            byte challenge[] = new byte[128];
                            secureRandom.nextBytes(challenge);
                            System.out.println("challenge: " + Util.ByteArrayToHexString(challenge));
                            byte[] signature = sign(smartcardIO, challenge, slotSignature);
                            if (signature != null) {
                                System.out.println("signature: " + Util.ByteArrayToHexString(signature));
                                Signature sig = Signature.getInstance("NONEwithRSA");
                                sig.initVerify(keycert);
                                sig.update(challenge);
                                boolean responseOK = sig.verify(signature);

                                System.out.println(responseOK ? "challenge/response OK" : "Invalid");
                                if (responseOK) {
                                    if (pin != null) {
                                        pin.pulse(1000, true); // set second argument to 'true' use a blocking call
                                    }
                                }
                            } else {
                                System.out.println("signature is null!");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Waiting for card absence...");
                smartcardIO.waitForCardAbsent();
                smartcardIO.teardown();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
