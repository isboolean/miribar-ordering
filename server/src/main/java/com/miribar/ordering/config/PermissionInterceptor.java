package com.miribar.ordering.config;

import com.miribar.ordering.common.JwtUtil;
import com.miribar.ordering.common.RequirePermission;
import com.miribar.ordering.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 权限校验拦截器
 * 校验 @RequirePermission 注解标记的接口
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper JSON = new ObjectMapper();
    
    private final JwtUtil jwtUtil;

    public PermissionInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 不是控制器方法，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 获取方法上的 @RequirePermission 注解
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            // 没有注解，放行
            return true;
        }
        
        // 获取需要的权限
        String requiredPermission = annotation.value();
        
        // 从请求头获取 token
        String authorization = request.getHeader("Authorization");
        String token = extractToken(authorization);
        String payload = token == null ? null : jwtUtil.verify(token);
        
        if (payload == null) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }
        
        // 检查用户是否有对应权限
        boolean hasPermission = jwtUtil.hasPermission(payload, requiredPermission);
        if (!hasPermission) {
            writeError(response, 403, "无权限访问，需要权限：" + requiredPermission);
            return false;
        }
        
        return true;
    }
    
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
    
    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(
                JSON.writeValueAsString(Result.error(code, message)).getBytes(StandardCharsets.UTF_8));
    }
}
