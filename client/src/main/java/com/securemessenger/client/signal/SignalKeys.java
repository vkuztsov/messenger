package com.securemessenger.client.signal;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;

public class SignalKeys {

    private IdentityKeyPair identityKeyPair;
    private PreKeyRecord preKeyRecord;
    private SignedPreKeyRecord signedPreKeyRecord;

    public SignalKeys() throws InvalidKeyException {
        identityKeyPair = KeyHelper.generateIdentityKeyPair();
        preKeyRecord = KeyHelper.generatePreKeys(1,1).get(0);
        signedPreKeyRecord = KeyHelper.generateSignedPreKey(identityKeyPair, 1);
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public PreKeyRecord getPreKeyRecord() {
        return preKeyRecord;
    }

    public SignedPreKeyRecord getSignedPreKeyRecord() {
        return signedPreKeyRecord;
    }

    public PreKeyBundle getKeyBundle(int regId, int deviceId) {
        return new PreKeyBundle(
                regId,
                regId,
                deviceId,
                preKeyRecord.getKeyPair().getPublicKey(),
                deviceId,
                signedPreKeyRecord.getKeyPair().getPublicKey(),
                signedPreKeyRecord.getSignature(),
                identityKeyPair.getPublicKey()
        );
    }
}
