package com.miribar.auth.controller;

import com.miribar.auth.service.CaptchaService;
import com.miribar.auth.service.SmsCodeService;
import com.miribar.common.dto.*;
import com.miribar.common.entity.Customer;
import com.miribar.common.entity.Staff;
import com.miribar.common.jwt.JwtUtil;
import com.miribar.auth.mapper.CustomerMapper;
import com.miribar.auth.mapper.StaffMapper;
import com.miribar.common.password.PasswordUtil;
import com.miribar.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 员工登录：账号密码 / 手机号短信验证码
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
        String token = jwtUtil.generateToken(staff.getId(), staff.getUsername(), staff.getRealName(), staff.getRole(), staff.getStoreId());
        return Result.success(toVO(staff, token));
    }

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
        String token = jwtUtil.generateToken(staff.getId(), staff.getUsername(), staff.getRealName(), staff.getRole(), staff.getStoreId());
        return Result.success(toVO(staff, token));
    }

    @GetMapping("/me")
    public Result<LoginVO> me(@RequestHeader(value = "X-Staff-Id", required = false) String staffIdHeader) {
        if (staffIdHeader == null || staffIdHeader.isEmpty() || "null".equals(staffIdHeader)) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        Long staffId = Long.parseLong(staffIdHeader);
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null || staff.getStatus() == null || staff.getStatus() != 1) {
            return Result.error(401, "账号不存在或已被禁用");
        }
        return Result.success(toVO(staff, null));
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
}
