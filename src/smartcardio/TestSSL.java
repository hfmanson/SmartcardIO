package smartcardio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class TestSSL {

    public static SSLSocketFactory hwKeyStore(String configName, char pin[]) {
        SSLSocketFactory sf = null;
        try {
//            Provider p = new sun.security.pkcs11.SunPKCS11(configName);
//            Security.addProvider(p);
//            KeyStore ks = KeyStore.getInstance("PKCS11");
            Provider p = new SimProvider();
            Security.addProvider(p);
            KeyStore ks = KeyStore.getInstance("SIM");
            ks.load(null, pin);
            SSLContext sc = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, pin);
            sc.init(kmf.getKeyManagers(), null, null);
            sf = sc.getSocketFactory();
        } catch (KeyStoreException ex) {
            Logger.getLogger(TestSSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestSSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TestSSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(TestSSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(TestSSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(TestSSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sf;
    }

    public static void testSSL(String urlString, String configName, char pin[]) {
        try {
            System.out.println("Visiting: " + urlString);
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            HttpsURLConnection httpsURLConnection = null;
            if (urlConnection instanceof HttpsURLConnection) {
                httpsURLConnection = (HttpsURLConnection) urlConnection;
                if (configName != null && pin != null) {
                    httpsURLConnection.setSSLSocketFactory(hwKeyStore(configName, pin));
                }
            }
            urlConnection.connect();
            if (httpsURLConnection != null) {
                System.out.println(httpsURLConnection.getCipherSuite());
                Certificate certificates[] = httpsURLConnection.getLocalCertificates();
                if (certificates != null) {
                    for (Certificate certificate : certificates) {
                        System.out.println(certificate);

                    }
                }
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(SmartcardIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        if (args.length != 0 && args.length != 2) {
            System.err.println("Usage: java smartcardio.TestSSL \n\tor java smartcardio..TestSSL <PKCS11 config file> <keystore password>");
            return;
        }
        final String configName = args.length == 2 ? args[0] : null;
        final char password[] = args.length == 2 ? args[1].toCharArray() : null;
        testSSL("https://www.mansoft.nl/protected/env.php", configName, password);
    }
}
