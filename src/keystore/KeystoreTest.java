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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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

    public static void checksignature() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            File f = new File("c:\\Users\\hfman\\Documents\\surfnet\\mansoft-ca.p12");
            InputStream is = new FileInputStream(f);
            ks.load(is, PASSWORD);
            Certificate certificate = ks.getCertificate("sim5");
            System.out.println("challenge length = " + CHALLENGE.length);
            System.out.println("signature length = " + SIGNATURE.length);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, certificate);
            byte[] result = cipher.doFinal(SIGNATURE);
            System.out.println("result.length = " + result.length);
            //System.out.println(ByteArrayToHexString(result));
            System.out.println(Arrays.equals(result, CHALLENGE) ? "OK" : "Invalid");
        } catch (KeyStoreException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
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

    public static void test2() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            File f = new File("c:\\Users\\hfman\\Documents\\surfnet\\mansoft-ca.p12");
            InputStream is = new FileInputStream(f);
            ks.load(is, PASSWORD);
            PrivateKey key = (PrivateKey) ks.getKey("secret", PASSWORD);
            System.err.println(key.getFormat());
            System.err.println(key);
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

    public static void main(String[] args) {
        checksignature();
    }
}
