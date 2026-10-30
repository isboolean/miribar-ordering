package com.miribar.ordering.controller;

import com.miribar.ordering.common.JwtUtil;
import com.miribar.ordering.common.PasswordUtil;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.dto.LoginRequest;
import com.miribar.ordering.dto.LoginVO;
import com.miribar.ordering.dto.StaffSmsLoginRequest;
import com.miribar.ordering.dto.StaffSmsRequest;
import com.miribar.ordering.entity.Role;
import com.miribar.ordering.entity.Staff;
import com.miribar.ordering.mapper.StaffMapper;
import com.miribar.ordering.service.CaptchaService;
import com.miribar.ordering.service.MenuService;
import com.miribar.ordering.service.RoleService;
import com.miribar.ordering.service.SmsCodeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工登录：账号密码（+图形验证码）/ 手机号短信验证码（发送前需图形验证码）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StaffMapper staffMapper;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;
    private final SmsCodeService smsCodeService;
    private final RoleService roleService;
    private final MenuService menuService;

    public AuthController(StaffMapper staffMapper, JwtUtil jwtUtil,
                          CaptchaService captchaService, SmsCodeService smsCodeService,
                          RoleService roleService, MenuService menuService) {
        this.staffMapper = staffMapper;
        this.jwtUtil = jwtUtil;
        this.captchaService = captchaService;
        this.smsCodeService = smsCodeService;
        this.roleService = roleService;
        this.menuService = menuService;
    }

    /** 方式一：账号密码登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            return Result.error(400, "图形验证码错误或已过期");
        }
        Staff staff = staffMapper.selectByUsername(request.getUsername().trim());
        if (staff == null || !PasswordUtil.matches(request.getPassword(), staff.getPassword())) {
            return Result.error(401, "账号或密码错误");
        }
        if (staff.getStatus() == null || staff.getStatus() != 1) {
            return Result.error(403, "账号已被禁用，请联系管理员");
        }
        return Result.success(buildLoginVO(staff));
    }

    /** 方式二第一步：发送短信验证码（需图形验证码，手机号须已绑定员工） */
    @PostMapping("/staff/sms")
    public Result<String> sendStaffSms(@Valid @RequestBody StaffSmsRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            return Result.error(400, "图形验证码错误或已过期");
        }
        String phone = request.getPhone().trim();
        Staff staff = staffMapper.selectByPhone(phone);
        if (staff == null) {
            return Result.error(404, "该手机号未绑定员工账号");
        }
        if (staff.getStatus() == null || staff.getStatus() != 1) {
            return Result.error(403, "账号已被禁用，请联系管理员");
        }
        String code = smsCodeService.sendCode("staff:" + phone);
        return Result.success(smsCodeService.isDevMode() ? code : null);
    }

    /** 方式二第二步：短信验证码登录 */
    @PostMapping("/staff/sms-login")
    public Result<LoginVO> smsLogin(@Valid @RequestBody StaffSmsLoginRequest request) {
        String phone = request.getPhone().trim();
        if (!smsCodeService.verify("staff:" + phone, request.getCode().trim())) {
            return Result.error(401, "验证码错误或已过期");
        }
        smsCodeService.consume("staff:" + phone);
        Staff staff = staffMapper.selectByPhone(phone);
        if (staff == null) {
            return Result.error(404, "该手机号未绑定员工账号");
        }
        if (staff.getStatus() == null || staff.getStatus() != 1) {
            return Result.error(403, "账号已被禁用，请联系管理员");
        }
        return Result.success(buildLoginVO(staff));
    }

    @GetMapping("/me")
    public Result<LoginVO> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        String payload = token == null ? null : jwtUtil.verify(token);
        if (payload == null) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        Staff staff = staffMapper.selectById(jwtUtil.getStaffId(payload));
        if (staff == null || staff.getStatus() == null || staff.getStatus() != 1) {
            return Result.error(401, "账号不存在或已被禁用");
        }
        LoginVO vo = buildLoginVO(staff);
        vo.setToken(token); // 保持原有 token
        return Result.success(vo);
    }
    
    /**
     * 获取当前用户菜单树
     */
    @GetMapping("/menu")
    public Result<List<Map<String, Object>>> getUserMenus(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        String payload = token == null ? null : jwtUtil.verify(token);
        if (payload == null) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        Long staffId = jwtUtil.getStaffId(payload);
        List<Role> roles = roleService.getRolesByStaffId(staffId);
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        List<Map<String, Object>> menus = menuService.getMenuTreeByRoleIds(roleIds);
        return Result.success(menus);
    }
    
    /**
     * 获取当前用户权限列表
     */
    @GetMapping("/permission")
    public Result<List<String>> getUserPermissions(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        String payload = token == null ? null : jwtUtil.verify(token);
        if (payload == null) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        List<String> permissions = jwtUtil.getPermissions(payload);
        return Result.success(permissions);
    }
    
    /**
     * 构建登录结果（包含权限和菜单）
     */
    private LoginVO buildLoginVO(Staff staff) {
        // 获取用户角色
        List<Role> roles = roleService.getRolesByStaffId(staff.getId());
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        
        // 获取权限列表
        List<String> permissions = menuService.getPermissionsByRoleIds(roleIds);
        
        // 获取菜单树
        List<Map<String, Object>> menus = menuService.getMenuTreeByRoleIds(roleIds);
        
        // 生成 token（包含权限列表）
        String token = jwtUtil.generateToken(
                staff.getId(), staff.getUsername(), staff.getRealName(), 
                staff.getRole(), staff.getStoreId(), permissions);
        
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setStaffId(staff.getId());
        vo.setStoreId(staff.getStoreId());
        vo.setUsername(staff.getUsername());
        vo.setRealName(staff.getRealName());
        vo.setRole(staff.getRole());
        vo.setPermissions(permissions);
        vo.setMenus(menus);
        return vo;
    }

    public static String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
}
