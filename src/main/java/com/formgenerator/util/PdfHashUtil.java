package com.formgenerator.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class PdfHashUtil {

    private PdfHashUtil() {}

    public static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static String sha256Base64(byte[] data) {
        return Base64.getEncoder().encodeToString(sha256(data));
    }
}
