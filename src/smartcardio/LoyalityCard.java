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
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;

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

    public static byte[] challenge(SmartcardIO smartcardIO, byte[] challenge) throws CardException {
        byte[] signature = null;
        CommandAPDU c = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_LOYALTY_CARD_AID);
        ResponseAPDU rapdu = smartcardIO.runAPDU(c);
        if (rapdu.getSW() == 0x9000) {
            c = new CommandAPDU(0x00, 0x56, 0x00, 0x00, challenge, 0x80);
            rapdu = smartcardIO.runAPDU(c);
            byte[] signatureLow = rapdu.getData();
            //System.out.println("sig1: " + Util.ByteArrayToHexString(signatureLow));
            if (rapdu.getSW() == 0x9000) {
                c = new CommandAPDU(0x00, 0x57, 0x00, 0x80, new byte[] { }, 0x80);
                rapdu = smartcardIO.runAPDU(c);
                if (rapdu.getSW() == 0x9000) {
                    byte[] signatureHigh = rapdu.getData();
                    //System.out.println("sig2: " + Util.ByteArrayToHexString(signatureHigh));
                    signature = new byte[0x100];
                    System.arraycopy(signatureLow, 0, signature, 0, 0x80);
                    System.arraycopy(signatureHigh, 0, signature, 0x80, 0x80);
                }
            }
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

    public static void main(String[] args) {

        try {
            SmartcardIO smartcardIO = new SmartcardIO();
            smartcardIO.debug = false;
            if (args.length == 0) {
                List<CardTerminal> terminals = smartcardIO.listTerminals();
                System.out.println("Terminals: " + terminals);
                System.exit(0);
            }
            X509Certificate slot = CertificateFromFileName(args[0]);
            X509Certificate sim = CertificateFromFileName(args[1]);

            byte[] slotFingerprint = getThumbprint(slot);
            byte[] keyFingerprint = getThumbprint(sim);
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
                        byte[] data = rapdu.getData();
                        System.out.println("received data: " + Util.ByteArrayToHexString(data));
                        byte[] receivedKeyFingerprint = Arrays.copyOfRange(data, 0, FINGERPRINT_LENGTH);
                        System.out.println("received key fingerprint: " + Util.ByteArrayToHexString(receivedKeyFingerprint));
                        byte[] random = Arrays.copyOfRange(data, FINGERPRINT_LENGTH, data.length);
                        System.out.println("received random bytes: " + Util.ByteArrayToHexString(random));
                        if (Arrays.equals(receivedKeyFingerprint, keyFingerprint)) {
                            System.out.println("Key fingerprint OK!");
                            //byte[] challenge = Util.HexStringToByteArray("3051300D060960864801650304020305000440FC936E9CE8B5250339585207FE555300FA2428F8CCCD3A28C704ED3D332D6565BDF440427BBE4E0F2EA9ED3268CE537ABD56434D0B930BDF72064518CD8DD825");
                            SecureRandom secureRandom = new SecureRandom();
                            byte challenge[] = new byte[128];
                            secureRandom.nextBytes(challenge);
                            System.out.println("challenge: " + Util.ByteArrayToHexString(challenge));
                            byte[] signature = challenge(smartcardIO, challenge);
                            if (signature != null) {
                                System.out.println("signature: " + Util.ByteArrayToHexString(signature));
                                Cipher cipher = Cipher.getInstance("RSA");
                                cipher.init(Cipher.DECRYPT_MODE, sim);
                                byte[] result = cipher.doFinal(signature);
                                System.out.println("result.length = " + result.length);
                                //System.out.println(Util.ByteArrayToHexString(result));
                                boolean responseOK = Arrays.equals(result, challenge);
                                System.out.println(responseOK ? "challenge/response OK" : "Invalid");
                                if (responseOK) {
                                    if (pin != null) {
                                        pin.pulse(1000, true); // set second argument to 'true' use a blocking call
                                    }
                                }
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
