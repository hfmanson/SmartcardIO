package smartcardio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimKeystorePKCS11 extends KeyStoreSpi {
    private KeyStore ks;

    public SimKeystorePKCS11() {
        try {
            Provider p = new sun.security.pkcs11.SunPKCS11("Configurations\\pkcs11.conf");
            Security.addProvider(p);
            //Util.printProviders();
            ks = KeyStore.getInstance("PKCS11");
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        Key key = null;
        try {
            key = ks.getKey(alias, password);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new SimPrivateKey((PrivateKey) key);
//        return key;
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        Certificate[] certificateChain = null;
        try {
            certificateChain = ks.getCertificateChain(alias);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return certificateChain;
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        Certificate certificate = null;
        try {
            certificate = ks.getCertificate(alias);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return certificate;
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        Date creationDate = null;
        try {
            creationDate = ks.getCreationDate(alias);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return creationDate;
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        ks.setKeyEntry(alias, key, password, chain);
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        ks.setKeyEntry(alias, key, chain);
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        ks.setCertificateEntry(alias, cert);
    }

    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        ks.deleteEntry(alias);
    }

    @Override
    public Enumeration<String> engineAliases() {
        Enumeration<String> aliases = null;
        try {
            aliases = ks.aliases();
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return aliases;
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        boolean containsAlias = false;
        try {
            containsAlias = ks.containsAlias(alias);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return containsAlias;
    }

    @Override
    public int engineSize() {
        int size = 0;
        try {
            size = ks.size();
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return size;
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        boolean isKeyEntry = false;
        try {
            isKeyEntry = ks.isKeyEntry(alias);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isKeyEntry;
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        boolean isCertificateEntry = false;
        try {
            isCertificateEntry = ks.isCertificateEntry(alias);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isCertificateEntry;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        String getCertificateAlias = null;
        try {
            getCertificateAlias = ks.getCertificateAlias(cert);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getCertificateAlias;
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        try {
            ks.store(stream, password);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SimKeystore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        ks.load(stream, password);
    }


}
