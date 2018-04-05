/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartcardio;

import java.security.PrivateKey;
import javax.security.auth.DestroyFailedException;

/**
 *
 * @author hfman
 */
public class SimPrivateKey implements PrivateKey {

    PrivateKey privateKey;

    public SimPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }



    @Override
    public String getAlgorithm() {
        return privateKey.getAlgorithm();
    }

    @Override
    public String getFormat() {
        return privateKey.getFormat();
    }

    @Override
    public byte[] getEncoded() {
        return privateKey.getEncoded();
    }

    @Override
    public void destroy() throws DestroyFailedException {
        privateKey.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return privateKey.isDestroyed();
    }

}
