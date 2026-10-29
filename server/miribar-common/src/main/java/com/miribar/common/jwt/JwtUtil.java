package com.miribar.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类（基于 JJWT 库）
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:miribar-ordering-default-secret-change-me-in-production-2026}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 确保密钥长度至少 256 位
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成员工 Token */
    public String generateToken(Long staffId, String username, String realName, Integer role, Long storeId) {
        return Jwts.builder()
                .subject(String.valueOf(staffId))
                .claims(Map.of(
                        "staffId", staffId,
                        "username", username,
                        "realName", realName != null ? realName : "",
                        "role", role,
                        "storeId", storeId,
                        "type", "staff"
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireHours * 3600 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /** 生成顾客 Token */
    public String generateCustomerToken(Long customerId, String phone) {
        return Jwts.builder()
                .subject(String.valueOf(customerId))
                .claims(Map.of(
                        "customerId", customerId,
                        "phone", phone,
                        "type", "customer"
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireHours * 3600 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /** 校验 Token，成功返回 Claims，失败返回 null */
    public Claims verify(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getStaffId(Claims claims) {
        return claims.get("staffId", Long.class);
    }

    public Long getCustomerId(Claims claims) {
        return claims.get("customerId", Long.class);
    }

    public Integer getRole(Claims claims) {
        return claims.get("role", Integer.class);
    }

    public Long getStoreId(Claims claims) {
        return claims.get("storeId", Long.class);
    }

    public String getTokenType(Claims claims) {
        return claims.get("type", String.class);
    }
}
