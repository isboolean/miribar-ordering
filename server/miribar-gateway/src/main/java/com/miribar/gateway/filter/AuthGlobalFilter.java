package com.miribar.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局鉴权过滤器 - 替代原 AuthInterceptor
 * 校验 JWT，将用户信息通过 Header 传递给下游服务
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Value("${jwt.secret:miribar-ordering-default-secret-change-me-in-production-2026}")
    private String secret;

    /** 顾客端开放路径 */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/captcha",
            "/api/auth/staff/sms",
            "/api/auth/staff/sms-login",
            "/api/auth/customer/**",
            "/api/menu/**",
            "/api/store/*",
            "/api/table/qr/*"
    );

    /** 顾客端 GET 开放路径 */
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/table/*",
            "/api/order/table/*",
            "/api/order/*"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        // WebSocket 路由直接放行
        if (path.startsWith("/ws/")) {
            return chain.filter(exchange);
        }

        // 开放路径放行
        if (isPublic(path, method)) {
            return chain.filter(exchange);
        }

        // JWT 校验
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeError(exchange, 401, "未登录或登录已过期");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return writeError(exchange, 401, "未登录或登录已过期");
        }

        try {
            Claims claims = verifyToken(token);
            if (claims == null) {
                return writeError(exchange, 401, "登录已过期，请重新登录");
            }

            // 管理端权限校验
            if (path.startsWith("/api/admin/")) {
                Integer role = claims.get("role", Integer.class);
                if (role == null || role < 2) {
                    return writeError(exchange, 403, "无权限访问，仅店长或平台管理员可操作");
                }
            }

            // 将用户信息注入请求 Header，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Staff-Id", String.valueOf(claims.get("staffId", Long.class) != null ? claims.get("staffId", Long.class) : ""))
                    .header("X-Customer-Id", String.valueOf(claims.get("customerId", Long.class) != null ? claims.get("customerId", Long.class) : ""))
                    .header("X-Role", String.valueOf(claims.get("role", Integer.class) != null ? claims.get("role", Integer.class) : ""))
                    .header("X-Store-Id", String.valueOf(claims.get("storeId", Long.class) != null ? claims.get("storeId", Long.class) : ""))
                    .header("X-Token-Type", String.valueOf(claims.get("type", String.class) != null ? claims.get("type", String.class) : ""))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("JWT 校验异常", e);
            return writeError(exchange, 401, "登录已过期，请重新登录");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(String path, String method) {
        // 公开路径完全放行
        for (String p : PUBLIC_PATHS) {
            if (MATCHER.match(p, path)) {
                return true;
            }
        }
        // GET 请求的公开路径
        if ("GET".equalsIgnoreCase(method)) {
            for (String p : PUBLIC_GET_PATHS) {
                if (MATCHER.match(p, path)) {
                    return true;
                }
            }
            // 顾客下单 POST 也放行
            if ("POST".equalsIgnoreCase(method) && MATCHER.match("/api/order", path)) {
                return true;
            }
        }
        // POST /api/order 顾客下单（可能携带顾客token或无token）
        if ("POST".equalsIgnoreCase(method) && MATCHER.match("/api/order", path)) {
            return true;
        }
        return false;
    }

    private Claims verifyToken(String token) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> writeError(ServerWebExchange exchange, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
