package com.amaravathi.tradeidentity.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {
    private static final SecureRandom RAND = new SecureRandom();

    public static String opaqueToken() {
        byte[] bytes = new byte[64];
        RAND.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
