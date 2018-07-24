/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartcardio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hfman
 */
public class SimKeystoreTest {

    private SimKeystore instance;

    public SimKeystoreTest() {
        try {
            instance = new SimKeystore();
            instance.engineLoad(null, new char[] { '1', '2', '3' ,'4' });
        } catch (IOException ex) {
            Logger.getLogger(SimKeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SimKeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(SimKeystoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of engineGetKey method, of class SimKeystore.
     */
    @Test
    public void testEngineGetKey() throws Exception {
        System.out.println("engineGetKey");
        PrivateKey key  = (PrivateKey) instance.engineGetKey("Certificate", null);
//        String alias = "";
//        char[] password = null;
//        SimKeystore instance = new SimKeystore();
//        Key expResult = null;
//        Key result = instance.engineGetKey(alias, password);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of engineGetCertificateChain method, of class SimKeystore.
     */
    @Test
    public void testEngineGetCertificateChain() {
        System.out.println("engineGetCertificateChain");
//        String alias = "";
//        SimKeystore instance = new SimKeystore();
//        Certificate[] expResult = null;
//        Certificate[] result = instance.engineGetCertificateChain(alias);
//        assertArrayEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineGetCertificate method, of class SimKeystore.
     */
    @Test
    public void testEngineGetCertificate() {
        System.out.println("engineGetCertificate");
        String alias = "Certificate";
        Certificate result = instance.engineGetCertificate(alias);
        PublicKey publicKey = result.getPublicKey();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of engineGetCreationDate method, of class SimKeystore.
     */
    @Test
    public void testEngineGetCreationDate() {
//        System.out.println("engineGetCreationDate");
//        String alias = "";
//        SimKeystore instance = new SimKeystore();
//        Date expResult = null;
//        Date result = instance.engineGetCreationDate(alias);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineSetKeyEntry method, of class SimKeystore.
     */
    @Test
    public void testEngineSetKeyEntry_4args() throws Exception {
//        System.out.println("engineSetKeyEntry");
//        String alias = "";
//        Key key = null;
//        char[] password = null;
//        Certificate[] chain = null;
//        SimKeystore instance = new SimKeystore();
//        instance.engineSetKeyEntry(alias, key, password, chain);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineSetKeyEntry method, of class SimKeystore.
     */
    @Test
    public void testEngineSetKeyEntry_3args() throws Exception {
//        System.out.println("engineSetKeyEntry");
//        String alias = "";
//        byte[] key = null;
//        Certificate[] chain = null;
//        SimKeystore instance = new SimKeystore();
//        instance.engineSetKeyEntry(alias, key, chain);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineSetCertificateEntry method, of class SimKeystore.
     */
    @Test
    public void testEngineSetCertificateEntry() throws Exception {
//        System.out.println("engineSetCertificateEntry");
//        String alias = "";
//        Certificate cert = null;
//        SimKeystore instance = new SimKeystore();
//        instance.engineSetCertificateEntry(alias, cert);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineDeleteEntry method, of class SimKeystore.
     */
    @Test
    public void testEngineDeleteEntry() throws Exception {
//        System.out.println("engineDeleteEntry");
//        String alias = "";
//        SimKeystore instance = new SimKeystore();
//        instance.engineDeleteEntry(alias);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineAliases method, of class SimKeystore.
     */
    @Test
    public void testEngineAliases() {
        System.out.println("engineAliases");
        List<String> expResult = new ArrayList<String>();
        expResult.addAll(Arrays.asList("a", "b"));
        //Enumeration<String> expResult = null;
        Enumeration<String> result = instance.engineAliases();
        while (result.hasMoreElements()) {
            System.out.println(result.nextElement());
        }
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of engineContainsAlias method, of class SimKeystore.
     */
    @Test
    public void testEngineContainsAlias() {
//        System.out.println("engineContainsAlias");
//        String alias = "";
//        SimKeystore instance = new SimKeystore();
//        boolean expResult = false;
//        boolean result = instance.engineContainsAlias(alias);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineSize method, of class SimKeystore.
     */
    @Test
    public void testEngineSize() {
        System.out.println("engineSize");
        int expResult = 1;
        int result = instance.engineSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of engineIsKeyEntry method, of class SimKeystore.
     */
    @Test
    public void testEngineIsKeyEntry() {
//        System.out.println("engineIsKeyEntry");
//        String alias = "";
//        SimKeystore instance = new SimKeystore();
//        boolean expResult = false;
//        boolean result = instance.engineIsKeyEntry(alias);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineIsCertificateEntry method, of class SimKeystore.
     */
    @Test
    public void testEngineIsCertificateEntry() {
//        System.out.println("engineIsCertificateEntry");
//        String alias = "";
//        SimKeystore instance = new SimKeystore();
//        boolean expResult = false;
//        boolean result = instance.engineIsCertificateEntry(alias);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineGetCertificateAlias method, of class SimKeystore.
     */
    @Test
    public void testEngineGetCertificateAlias() {
//        System.out.println("engineGetCertificateAlias");
//        Certificate cert = null;
//        SimKeystore instance = new SimKeystore();
//        String expResult = "";
//        String result = instance.engineGetCertificateAlias(cert);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineStore method, of class SimKeystore.
     */
    @Test
    public void testEngineStore() throws Exception {
//        System.out.println("engineStore");
//        OutputStream stream = null;
//        char[] password = null;
//        SimKeystore instance = new SimKeystore();
//        instance.engineStore(stream, password);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of engineLoad method, of class SimKeystore.
     */
    @Test
    public void testEngineLoad() throws Exception {
//        System.out.println("engineLoad");
//        InputStream stream = null;
//        char[] password = null;
//        SimKeystore instance = new SimKeystore();
//        instance.engineLoad(stream, password);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}
