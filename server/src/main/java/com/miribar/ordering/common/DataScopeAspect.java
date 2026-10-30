package com.miribar.ordering.common;

import com.miribar.ordering.controller.AuthController;
import com.miribar.ordering.entity.Role;
import com.miribar.ordering.entity.Staff;
import com.miribar.ordering.mapper.StaffMapper;
import com.miribar.ordering.service.OrgService;
import com.miribar.ordering.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 数据权限切面
 * 拦截带有 @DataScope 注解的方法，根据用户角色的数据范围过滤数据
 */
@Aspect
@Component
public class DataScopeAspect {
    
    private final JwtUtil jwtUtil;
    private final StaffMapper staffMapper;
    private final RoleService roleService;
    private final OrgService orgService;
    
    // ThreadLocal 存储当前用户的组织范围
    private static final ThreadLocal<List<Long>> ORG_SCOPE = new ThreadLocal<>();
    
    public DataScopeAspect(JwtUtil jwtUtil, StaffMapper staffMapper, 
                          RoleService roleService, OrgService orgService) {
        this.jwtUtil = jwtUtil;
        this.staffMapper = staffMapper;
        this.roleService = roleService;
        this.orgService = orgService;
    }
    
    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint point, DataScope dataScope) throws Throwable {
        try {
            // 获取当前用户信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return point.proceed();
            }
            
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            String token = extractToken(authorization);
            String payload = token == null ? null : jwtUtil.verify(token);
            
            if (payload == null) {
                return point.proceed();
            }
            
            Long staffId = jwtUtil.getStaffId(payload);
            Staff staff = staffMapper.selectById(staffId);
            if (staff == null || staff.getOrgId() == null) {
                return point.proceed();
            }
            
            // 获取用户角色
            List<Role> roles = roleService.getRolesByStaffId(staffId);
            if (roles.isEmpty()) {
                return point.proceed();
            }
            
            // 计算数据范围（取所有角色中最宽的范围）
            Integer minDataScope = roles.stream()
                .map(Role::getDataScope)
                .min(Integer::compareTo)
                .orElse(1);
            
            List<Long> orgIds = null;
            
            if (minDataScope == 1) {
                // 全部数据，不限制
                orgIds = null;
            } else if (minDataScope == 2) {
                // 本组织及下级
                orgIds = orgService.getOrgTreeIds(staff.getOrgId());
            } else if (minDataScope == 3) {
                // 仅本组织
                orgIds = List.of(staff.getOrgId());
            }
            
            ORG_SCOPE.set(orgIds);
            
            return point.proceed();
        } finally {
            ORG_SCOPE.remove();
        }
    }
    
    /**
     * 获取当前用户的组织范围（供 Service 层调用）
     */
    public static List<Long> getCurrentOrgScope() {
        return ORG_SCOPE.get();
    }
    
    private String extractToken(String authorization) {
        return AuthController.extractToken(authorization);
    }
}
