package com.miribar.ordering.controller;

import com.miribar.ordering.common.JwtUtil;
import com.miribar.ordering.common.PasswordUtil;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.dto.LoginRequest;
import com.miribar.ordering.dto.LoginVO;
import com.miribar.ordering.dto.StaffSmsLoginRequest;
import com.miribar.ordering.dto.StaffSmsRequest;
import com.miribar.ordering.entity.Staff;
import com.miribar.ordering.mapper.StaffMapper;
import com.miribar.ordering.service.CaptchaService;
import com.miribar.ordering.service.SmsCodeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

    public AuthController(StaffMapper staffMapper, JwtUtil jwtUtil,
                          CaptchaService captchaService, SmsCodeService smsCodeService) {
        this.staffMapper = staffMapper;
        this.jwtUtil = jwtUtil;
        this.captchaService = captchaService;
        this.smsCodeService = smsCodeService;
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
        String token = jwtUtil.generateToken(
                staff.getId(), staff.getUsername(), staff.getRealName(), staff.getRole(), staff.getStoreId());
        return Result.success(toVO(staff, token));
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
        String token = jwtUtil.generateToken(
                staff.getId(), staff.getUsername(), staff.getRealName(), staff.getRole(), staff.getStoreId());
        return Result.success(toVO(staff, token));
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
        return Result.success(toVO(staff, token));
    }

    private LoginVO toVO(Staff staff, String token) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setStaffId(staff.getId());
        vo.setStoreId(staff.getStoreId());
        vo.setUsername(staff.getUsername());
        vo.setRealName(staff.getRealName());
        vo.setRole(staff.getRole());
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
