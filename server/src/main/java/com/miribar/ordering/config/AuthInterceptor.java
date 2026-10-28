package com.miribar.ordering.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miribar.ordering.common.JwtUtil;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 员工认证拦截器
 * - 顾客端（菜单/下单/桌台查询）保持开放
 * - 管理端接口需登录，且仅店长(2)/平台管理员(3)可访问
 * - 吧台/后厨相关接口需任意员工登录
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final ObjectMapper JSON = new ObjectMapper();

    /** 需要登录（任意员工）的路径 */
    private static final List<String> STAFF_PATHS = List.of(
            "/api/admin/**",
            "/api/order/*/accept",
            "/api/order/*/complete",
            "/api/order/*/reject",
            "/api/order/*/cancel",
            "/api/order/store/*/active",
            "/api/order/store/*/status/*",
            "/api/table/*/open",
            "/api/table/*/clear",
            "/api/table/*/checkout"
    );

    /** 仅店长/平台管理员可访问的路径 */
    private static final List<String> ADMIN_PATHS = List.of(
            "/api/admin/**"
    );

    /** 顾客端仍可访问的路径（优先级高于 STAFF_PATHS） */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/captcha",          // 图形验证码
            "/api/auth/staff/sms",        // 员工短信登录：发验证码
            "/api/auth/staff/sms-login",  // 员工短信登录：提交验证码
            "/api/auth/customer/**",      // 顾客登录（控制器内自行校验顾客 token）
            "/api/menu/**",
            "/api/store/*",
            "/api/table/qr/*",
            "/api/order",             // 顾客下单 POST
            "/api/order/table/*",     // 顾客查看本桌订单
            "/api/order/*"            // 顾客查看订单详情 GET（子路径操作仍需员工登录）
    );

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 顾客端开放路径：放行（但管理端写操作仍拦截）
        if (isPublic(request, path)) {
            return true;
        }

        String token = AuthController.extractToken(request.getHeader("Authorization"));
        String payload = token == null ? null : jwtUtil.verify(token);
        if (payload == null) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }

        if (isAdminPath(path)) {
            Integer role = jwtUtil.getRole(payload);
            if (role == null || role < 2) {
                writeError(response, 403, "无权限访问，仅店长或平台管理员可操作");
                return false;
            }
        }
        return true;
    }

    private boolean isPublic(HttpServletRequest request, String path) {
        // /api/table/* 的 GET 查询对顾客开放，写操作仍需登录
        if ("GET".equals(request.getMethod()) && MATCHER.match("/api/table/*", path)) {
            return true;
        }
        return PUBLIC_PATHS.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(
                JSON.writeValueAsString(Result.error(code, message)).getBytes(StandardCharsets.UTF_8));
    }
}
