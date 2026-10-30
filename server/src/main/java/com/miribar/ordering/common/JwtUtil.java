package com.miribar.ordering.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 轻量 JWT 工具（HS256，基于 JDK 自带 HMAC，无第三方依赖）
 * Payload: { staffId, username, realName, role, storeId, permissions[], exp }
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:miribar-ordering-default-secret-change-me-in-production}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    public String generateToken(Long staffId, String username, String realName, Integer role, Long storeId, java.util.List<String> permissions) {
        long exp = System.currentTimeMillis() / 1000 + expireHours * 3600;
        StringBuilder permBuilder = new StringBuilder("[");
        if (permissions != null && !permissions.isEmpty()) {
            for (int i = 0; i < permissions.size(); i++) {
                if (i > 0) permBuilder.append(",");
                permBuilder.append("\"").append(escape(permissions.get(i))).append("\"");
            }
        }
        permBuilder.append("]");
        
        String payload = "{\"staffId\":" + staffId
                + ",\"username\":\"" + escape(username) + "\""
                + (realName == null ? "" : ",\"realName\":\"" + escape(realName) + "\"")
                + ",\"role\":" + role
                + ",\"storeId\":" + storeId
                + ",\"permissions\":" + permBuilder.toString()
                + ",\"exp\":" + exp + "}";
        return sign(payload);
    }
    
    // 向后兼容的旧方法
    public String generateToken(Long staffId, String username, String realName, Integer role, Long storeId) {
        return generateToken(staffId, username, realName, role, storeId, null);
    }

    /** 顾客 token：payload 含 customerId，与员工 token 区分 */
    public String generateCustomerToken(Long customerId, String phone) {
        long exp = System.currentTimeMillis() / 1000 + expireHours * 3600;
        String payload = "{\"customerId\":" + customerId
                + ",\"phone\":\"" + escape(phone) + "\""
                + ",\"type\":\"customer\""
                + ",\"exp\":" + exp + "}";
        return sign(payload);
    }

    private String sign(String payload) {
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String body = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        return header + "." + body + "." + hmac(header + "." + body);
    }

    /** 校验签名与有效期，通过则返回 Claims（JSON 字符串），失败返回 null */
    public String verify(String token) {
        if (token == null) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String expected = hmac(parts[0] + "." + parts[1]);
        if (!MessageDigestIsEqual(expected, parts[2])) {
            return null;
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Long exp = extractLong(payload, "exp");
        if (exp == null || System.currentTimeMillis() / 1000 >= exp) {
            return null;
        }
        return payload;
    }

    public Long getStaffId(String payload) {
        return extractLong(payload, "staffId");
    }

    public Long getCustomerId(String payload) {
        return extractLong(payload, "customerId");
    }

    public Integer getRole(String payload) {
        return extractLong(payload, "role") == null ? null : extractLong(payload, "role").intValue();
    }

    public Long getStoreId(String payload) {
        return extractLong(payload, "storeId");
    }
    
    /**
     * 获取用户权限列表
     */
    public java.util.List<String> getPermissions(String payload) {
        java.util.List<String> permissions = new java.util.ArrayList<>();
        if (payload == null) return permissions;
        
        int start = payload.indexOf("\"permissions\":[");
        if (start < 0) return permissions;
        
        start = payload.indexOf("[", start) + 1;
        int end = payload.indexOf("]", start);
        if (end < 0) return permissions;
        
        String permStr = payload.substring(start, end).trim();
        if (permStr.isEmpty()) return permissions;
        
        // 简单解析 JSON 数组
        String[] parts = permStr.split(",");
        for (String part : parts) {
            String perm = part.trim();
            if (perm.startsWith("\"") && perm.endsWith("\"")) {
                perm = perm.substring(1, perm.length() - 1);
                perm = perm.replace("\\\"", "\"").replace("\\\\", "\\");
                permissions.add(perm);
            }
        }
        return permissions;
    }
    
    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(String payload, String permission) {
        java.util.List<String> permissions = getPermissions(payload);
        return permissions.contains(permission);
    }

    private static Long extractLong(String json, String key) {
        int i = json.indexOf("\"" + key + "\":");
        if (i < 0) {
            return null;
        }
        int start = i + key.length() + 3;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)))) {
            end++;
        }
        if (end == start) {
            return null;
        }
        return Long.parseLong(json.substring(start, end));
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }

    private static boolean MessageDigestIsEqual(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
