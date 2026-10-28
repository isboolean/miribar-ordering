package com.miribar.ordering.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 密码加盐哈希工具（SHA-256）
 * 存储格式：sha256$<salt>$<hash>
 * 兼容历史明文数据：若存储值不符合哈希格式，则按明文比对（比对成功后建议更新为哈希）
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "sha256$";

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        byte[] salt = new byte[8];
        RANDOM.nextBytes(salt);
        String saltHex = toHex(salt);
        return PREFIX + saltHex + "$" + sha256Hex(saltHex + rawPassword);
    }

    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        if (!stored.startsWith(PREFIX)) {
            // 历史明文密码兼容
            return rawPassword.equals(stored);
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 3) {
            return false;
        }
        String computed = sha256Hex(parts[1] + rawPassword);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
