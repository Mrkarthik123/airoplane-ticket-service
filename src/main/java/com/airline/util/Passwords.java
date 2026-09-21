package com.airline.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** SHA-256 password hashing (demo-grade; use bcrypt in production). */
public final class Passwords {
    private Passwords() {}

    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String plain, String hash) {
        return hash(plain).equals(hash);
    }
}
