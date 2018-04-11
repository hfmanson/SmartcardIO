/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartcardio;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAPrivateKey;
import org.bouncycastle.asn1.DERObject;
import org.opensc.pkcs15.asn1.PKCS15Key;
import org.opensc.pkcs15.asn1.PKCS15PrivateKey;
import org.opensc.pkcs15.asn1.PKCS15RSAPrivateKey;
import org.opensc.pkcs15.asn1.attr.RSAPrivateKeyObject;
import org.opensc.pkcs15.asn1.proxy.ReferenceProxy;

/**
 *
 * @author hfman
 */
public class SimSignature extends SignatureSpi{

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (privateKey instanceof RSAPrivateKeyObject) {
            RSAPrivateKeyObject x = (RSAPrivateKeyObject) privateKey;
            DERObject y = x.getDERObject();
            ((ReferenceProxy<PKCS15PrivateKey>)privateKey).resolveEntity();

//            PKCS15PrivateKey z = PKCS15PrivateKey.getInstance(y.toASN1Object());
            System.out.println("OK: " + y);
        } else {
            throw new InvalidKeyException();
        }
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        return null;
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        return true;
    }

    @Override
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    }

    @Override
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        return null;
    }

}
