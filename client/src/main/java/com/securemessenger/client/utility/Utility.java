package com.securemessenger.client.utility;

import java.util.Base64;
import java.util.Random;

public class Utility {
    public static int generateDeviceId() {
        Random random = new Random();
        return random.nextInt(999999) + 1;
    }

    public static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] fromBase64(String str) {
        return Base64.getDecoder().decode(str);
    }
}
