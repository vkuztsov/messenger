package com.securemessenger.client.signal;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;

import java.io.IOException;
import java.util.Base64;

public class SignalUtility {
    public static String serializeKeyBundle(PreKeyBundle keyBundle) {
        String serializedData = keyBundle.getRegistrationId() + ":" + keyBundle.getDeviceId() + ":"
                + keyBundle.getPreKeyId() + ":" + Base64.getEncoder().encodeToString(keyBundle.getPreKey().serialize()) + ":" +
                keyBundle.getSignedPreKeyId() + ":" + Base64.getEncoder().encodeToString(keyBundle.getSignedPreKey().serialize()) +
                ":" + Base64.getEncoder().encodeToString(keyBundle.getSignedPreKeySignature()) + ":" +
                Base64.getEncoder().encodeToString(keyBundle.getIdentityKey().serialize());
        return serializedData;
    }

    public static PreKeyBundle deserializeKeyBundle(String base64KeyBundle) throws IOException, InvalidKeyException {
        String[] parts = base64KeyBundle.split(":");

        return new PreKeyBundle(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Curve.decodePoint(Base64.getDecoder().decode(parts[3]), 0),
                Integer.parseInt(parts[4]),
                Curve.decodePoint(Base64.getDecoder().decode(parts[5]), 0),
                Base64.getDecoder().decode(parts[6]),
                new IdentityKey(Base64.getDecoder().decode(parts[7]), 0)
        );
    }

    public static void saveProtocolStore(SignalProtocolStore signalProtocolStore, int preKeyId, int signedPreKeyId) throws InvalidKeyIdException {
        Configuration conf = new Configuration("protocol.store", "Protocol Store");
        conf.set("key_pair", Base64.getEncoder().encodeToString(signalProtocolStore.getIdentityKeyPair().serialize()));
        conf.set("id", String.valueOf(signalProtocolStore.getLocalRegistrationId()));
        conf.set("signed_pre_key_id", String.valueOf(signedPreKeyId));
        conf.set("pre_key_id", String.valueOf(preKeyId));
        conf.set("pre_key", Base64.getEncoder().encodeToString(signalProtocolStore.loadPreKey(preKeyId).serialize()));
        conf.set("signed_pre_key", Base64.getEncoder().encodeToString(signalProtocolStore.loadSignedPreKey(signedPreKeyId).serialize()));
    }

    public static SignalProtocolStore loadLocalProtocolStore() throws InvalidKeyException, IOException {
        Configuration conf = new Configuration("protocol.store", "Protocol Store");
        byte[] keyPair = Base64.getDecoder().decode(conf.get("key_pair"));
        byte[] preKey = Base64.getDecoder().decode(conf.get("pre_key"));
        byte[] signedPreKey = Base64.getDecoder().decode(conf.get("signed_pre_key"));
        int regId = Integer.parseInt(conf.get("id"));
        int preKeyId = Integer.parseInt(conf.get("pre_key_id"));
        int signedPreKeyId = Integer.parseInt(conf.get("signed_pre_key_id"));

        SignalProtocolStore store = new InMemorySignalProtocolStore(new IdentityKeyPair(keyPair), regId);
        store.storePreKey(preKeyId, new PreKeyRecord(preKey));
        store.storeSignedPreKey(signedPreKeyId, new SignedPreKeyRecord(signedPreKey));

        return store;
    }

    public static void setContactKeyBundle(String username, String base64KeyBundle) {
        Configuration conf = new Configuration("contacts.store", "Contacts Keys");
        conf.set(username, base64KeyBundle);
    }

    public static PreKeyBundle getContactKeyBundle(String username) throws IOException, InvalidKeyException {
        Configuration conf = new Configuration("contacts.store", "Contacts Keys");
        return deserializeKeyBundle(conf.get(username));
    }
}
