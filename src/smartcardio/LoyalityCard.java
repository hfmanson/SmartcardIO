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
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
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

    public static void main(String[] args) {

        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = true;
            if (args.length == 0) {
                List<CardTerminal> terminals = smartcardIO.listTerminals();
                System.out.println("Terminals: " + terminals);
                System.exit(0);
            }
            X509Certificate slot = CertificateFromFileName(args[0]);
            X509Certificate sim = CertificateFromFileName(args[1]);

            byte[] slotFingerprint = getThumbprint(slot);
            byte[] keyFingerprint = getThumbprint(sim);
            System.out.println("slot sig alg: " + slot.getSigAlgName());
            System.out.println("slot fingerprint: " + Util.ByteArrayToHexString(slotFingerprint));
            System.out.println("key issuer: " + sim.getIssuerDN().getName());
            System.out.println("key subject: " + sim.getSubjectDN().getName());
            System.out.println("key fingerprint: " + Util.ByteArrayToHexString(keyFingerprint));
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
                        System.out.println("received key fingerprint: " + Util.ByteArrayToHexString(receivedKeyFingerprint));
                        byte[] random = Arrays.copyOfRange(data, FINGERPRINT_LENGTH, data.length);
                        System.out.println("received random bytes: " + Util.ByteArrayToHexString(random));
                        KeyStore ks = KeyStore.getInstance("PKCS12");
                        File f = new File("slot1.p12");
                        InputStream is = new FileInputStream(f);
                        ks.load(is, PASSWORD);

                        byte[] slotSignature = signChallenge((PrivateKey) ks.getKey("slot1", PASSWORD), random);
                        System.out.println("slot signature: " + Util.ByteArrayToHexString(slotSignature));
                        if (Arrays.equals(receivedKeyFingerprint, keyFingerprint)) {
                            System.out.println("Key fingerprint OK!");
                            SecureRandom secureRandom = new SecureRandom();
                            byte challenge[] = new byte[128];
                            secureRandom.nextBytes(challenge);
                            System.out.println("challenge: " + Util.ByteArrayToHexString(challenge));
                            byte[] signature = sign(smartcardIO, challenge, slotSignature);
                            if (signature != null) {
                                System.out.println("signature: " + Util.ByteArrayToHexString(signature));
                                Signature sig = Signature.getInstance("NONEwithRSA");
                                sig.initVerify(sim);
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
