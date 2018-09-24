/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartcardio;

import java.util.Arrays;

/**
 *
 * @author hfman
 */
public class Fingerprint {
    byte[] fingerprint;

    public Fingerprint(byte[] fingerprint) {
        this.fingerprint = this.fingerprint;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Fingerprint ? Arrays.equals(fingerprint, ((Fingerprint) object).fingerprint) : false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Arrays.hashCode(this.fingerprint);
        return hash;
    }
}
