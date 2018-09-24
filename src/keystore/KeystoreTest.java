/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.smartcardio.CardException;

/**
 *
 * https://docs.google.com/document/d/1xsvT7M6wnuQ70k76MIV-RLHs8UP0bjIzWumtb-zuIRs/edit
 */
public class KeystoreTest {
    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        String result = "null";
        if (bytes != null) {
            final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
            char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
            int v;
            for (int j = 0; j < bytes.length; j++) {
                v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
                hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
                hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
            }
            result = new String(hexChars);
        }
        return result;
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws java.lang.IllegalArgumentException if input length is incorrect
     */
    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static final String CHALLENGE_STRING = "3051300D060960864801650304020305000440FC936E9CE8B5250339585207FE555300FA2428F8CCCD3A28C704ED3D332D6565BDF440427BBE4E0F2EA9ED3268CE537ABD56434D0B930BDF72064518CD8DD825";
    public static final String SIGNATURE_STRING = "845BBE214A556FB4CFE8B7192304185C48C4FA2B589D89BBCD8FF28D52A6BF76269745EF86191AC95D6834CD15860DDF99808EE11A7702394E98E8A38356698D1D04A16344F17B4115967712F32C507D1207223446F34419D8CE5ED8E56A12E0AD25C8BD23AA05A8D9E960A39477BA9DCBD43085CBE19FB74D0C3FCDFEBE0D0E584A4AF8A27EB4FB57D5C3626CE835FF5BB643E56FBFE76D16985AE1ADF2EDAEC92B89585662A9EFFFDFE4070D3EA05CFA283FD76B1E2A7E85481760B8585F2FE070463A12A1055C16F2D5D653C266F41FC922FFBD1247C8964085E0EE4E6999A2529B7A5C3345FE2F5F0E3CB46399D5BDAC8E4E09D41F50CAF494482CE1E515";
    public static final byte[] CHALLENGE = HexStringToByteArray(CHALLENGE_STRING);
    public static final byte[] SIGNATURE = HexStringToByteArray(SIGNATURE_STRING);
    public static final char[] PASSWORD = new char[] { 'h', 'e', 'n', 'r', 'i', '1', '2', '3', '4' };

    public static boolean verify(Certificate certificate, byte[] challenge, byte[] signature) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, certificate);
            byte[] result = cipher.doFinal(signature);
            System.out.println("result.length = " + result.length);
            //System.out.println(ByteArrayToHexString(result));
            return Arrays.equals(result, challenge);
    }

    public static boolean verify2(Certificate certificate, byte[] challenge, byte[] signature, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SignatureException, NoSuchProviderException {
        Signature sig = Signature.getInstance(algorithm);
        sig.initVerify(certificate);
        sig.update(challenge);
        System.out.println(sig);
        return sig.verify(signature);
    }

    public static Certificate getCertificateFromKeystore(String certificateName) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            File f = new File("c:\\Users\\hfman\\Documents\\surfnet\\mansoft-ca.p12");
            InputStream is = new FileInputStream(f);
            ks.load(is, PASSWORD);
            is.close();
            return ks.getCertificate(certificateName);
    }

    public static X509Certificate getCertificateFromFileName(String fileName) throws FileNotFoundException, CertificateException, IOException {
        X509Certificate result;
        try (FileInputStream is = new FileInputStream(fileName)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            result = (X509Certificate) certificateFactory.generateCertificate(is);
        }
        return result;
    }


    public static void checksignature(Certificate certificate, byte[] challenge, byte[] signature, String algorithm) throws SignatureException, NoSuchProviderException {
        try {
            System.out.println("challenge length = " + challenge.length);
            System.out.println("signature length = " + signature.length);
            System.out.println(verify2(certificate, challenge, signature, algorithm) ? "OK" : "Invalid");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static KeyStore getKeyStore() throws Exception{
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        File f = new File("mansoft-ca.p12");
        try (InputStream is = new FileInputStream(f)) {
            ks.load(is, PASSWORD);
        }
        return ks;
    }

    public static void testSign(String keyName, String algorithm, byte[] data) throws Exception {
        try {
            KeyStore ks = getKeyStore();
            PrivateKey key;
            if (keyName.equals("rsa2048")) {
                RSAPrivateKey rsakey = (RSAPrivateKey) ks.getKey(keyName, new char [] { '1', '6', '8', '5', '1', '7', '5', '0' });
                System.out.println(rsakey.getModulus().bitLength());
                key = rsakey;
            } else {
                DSAPrivateKey dsakey = (DSAPrivateKey) ks.getKey(keyName, new char [] { '1', '6', '8', '5', '1', '7', '5', '0' });
                key = dsakey;
            }
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(key);
            sig.update(data);
            System.out.println(sig);
            byte[] signature = sig.sign();
            System.out.println("signature.length = " + signature.length);
            System.out.println(ByteArrayToHexString(signature));
        } catch (IOException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static byte[] testEncrypt(Certificate certificate, byte[] data) {
        byte[] encrypted = null;
        try {
            System.out.println("data length: " + data.length);
            PublicKey pubkey = certificate.getPublicKey();
            String algorithm = pubkey.getAlgorithm();
            System.out.println("algorithm: " + algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            encrypted = cipher.doFinal(data);
            System.out.println("encrypted length: " + encrypted.length);
            System.out.println("encrypted: " + ByteArrayToHexString(encrypted));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return encrypted;
    }

    public static byte[] testDecrypt(PrivateKey privatekey, byte[] encrypted) throws Exception {
        System.out.println("testDecrypt");

        String algorithm = privatekey.getAlgorithm();
        System.out.println("Private key algorithm: " + algorithm);
        Cipher decryptCipher = Cipher.getInstance(algorithm);
        decryptCipher.init(Cipher.DECRYPT_MODE, privatekey);
        byte[] decrypted = decryptCipher.doFinal(encrypted);
        System.out.println("decrypted: " + ByteArrayToHexString(decrypted));
        return decrypted;
    }

    public static void testCipher(String alias) {
        try {
            KeyStore ks = getKeyStore();
            Certificate slot2 = ks.getCertificate(alias);
            byte[] encrypted = testEncrypt(slot2, new byte[] { 1, 2, 3, 4} );
            //encrypted[10] = 10;
            PrivateKey privatekey = (PrivateKey) ks.getKey(alias, new char [] { '1', '6', '8', '5', '1', '7', '5', '0' });
            testDecrypt(privatekey, encrypted);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    public static void testSignature() {
        try {
        Certificate sim5 = getCertificateFromKeystore("sim5");
        checksignature(sim5, CHALLENGE, SIGNATURE, "NONEwithRSA");
        Certificate slot2 = getCertificateFromFileName("slot2.cer");
        checksignature(
                slot2,
                HexStringToByteArray("89316B6211D867BCC7CCC8297C3FAC8834049661EE6611025FE252443D3415DBB4E60B88A06CBAA72E8534510B19C9BE06A0F24CF055F00FB7AA7EC70722F716BE381377973A658FEE6E2B16FAAB488528495A3FA0E4FC6C224BFB9E67E14BC92C939EF88EA745FAF5ABD6A9B3C2586AF749A4A974142A914BE3E24BD056E3E2"),
                HexStringToByteArray("303B021B2D960B0974337DE50DBFF0B40FD9856A778D549283067375CEE4EB021C76FCE7200B5D715DD08E57BCCDAB1845B0F5E7B455899883A4B02335"),
                "SHA256withDSA");
        Certificate sim923 = getCertificateFromFileName("sim923.cer");
        checksignature(
                sim923,
                HexStringToByteArray("48454E5249"),
                HexStringToByteArray("32076C8C4A82DE43033DEF40723F6362D4FDA7E70B22DB1B80049AC08303D5FA806422A8156B1E06E635B61EE5BEFE7CDCB0838D7769C3B706BE2611F31C7A93C8D6700459FFA7C67202EF9F9FBB8EA4AAB644A7D5E42E5ACCAFA41A9053AB96B3E72D102C9AC00AD454B5B596EEDDEDDEB8E41A1CEEDFF280DCCE416D51574FABC801107AEBD53F5DAB3830DC4DD20630D7F0075F2F8C8742E37614264BD843295CB258DF8022439BF106F9F0D8A889D56E4E8DD9C7FE169D417E61140EB20824AC507BA20D785F8C4BF72527C6FD85346DD2BFB87E080FB851B2D9214D5EDFB61A375C763E2E0A5BC14B88D1C5FE4489BBEA1288DB448ECAE53912151D49BF"),
                "NONEwithRSA");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        //testSign("rsa2048", "NONEwithRSA", new byte[] { 5, 6, 7, 8 });
        //testSign("dsa2048", "SHA256withDSA", new byte[128]);
    }


    public static void main(String[] args) throws Exception {
        //Certificate slot1 = getCertificateFromFileName("slot1.cer");
        //testEncrypt(slot1, new byte[256]);
        testCipher("mansoft-ca");
    }
}
