package com.miribar.auth.controller;

import com.miribar.auth.service.CaptchaService;
import com.miribar.auth.service.SmsCodeService;
import com.miribar.common.dto.*;
import com.miribar.common.entity.Customer;
import com.miribar.common.jwt.JwtUtil;
import com.miribar.auth.mapper.CustomerMapper;
import com.miribar.common.password.PasswordUtil;
import com.miribar.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 顾客端登录：手机号短信验证码 / 手机号密码
 */
@RestController
@RequestMapping("/api/auth/customer")
public class CustomerAuthController {

    private final SmsCodeService smsCodeService;
    private final CustomerMapper customerMapper;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;

    public CustomerAuthController(SmsCodeService smsCodeService, CustomerMapper customerMapper,
                                  JwtUtil jwtUtil, CaptchaService captchaService) {
        this.smsCodeService = smsCodeService;
        this.customerMapper = customerMapper;
        this.jwtUtil = jwtUtil;
        this.captchaService = captchaService;
    }

    @PostMapping("/sms")
    public Result<String> sendSms(@Valid @RequestBody SmsRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            return Result.error(400, "图形验证码错误或已过期");
        }
        String phone = request.getPhone().trim();
        String code = smsCodeService.sendCode("customer:" + phone);
        return Result.success(smsCodeService.isDevMode() ? code : null);
    }

    @PostMapping("/login")
    public Result<CustomerVO> login(@Valid @RequestBody CustomerLoginRequest request) {
        String phone = request.getPhone().trim();
        if (!smsCodeService.verify("customer:" + phone, request.getCode().trim())) {
            return Result.error(401, "验证码错误或已过期");
        }
        smsCodeService.consume("customer:" + phone);
        return Result.success(findOrCreateAndBuildVO(phone));
    }

    @PostMapping("/password-login")
    public Result<CustomerVO> passwordLogin(@Valid @RequestBody CustomerPasswordLoginRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            return Result.error(400, "图形验证码错误或已过期");
        }
        Customer customer = customerMapper.selectByPhone(request.getPhone().trim());
        if (customer == null || customer.getPassword() == null) {
            return Result.error(404, "该手机号未注册或未设置密码，请使用验证码登录");
        }
        if (!PasswordUtil.matches(request.getPassword(), customer.getPassword())) {
            return Result.error(401, "手机号或密码错误");
        }
        return Result.success(toVO(customer));
    }

    @PostMapping("/password/set")
    public Result<Void> setPassword(@Valid @RequestBody SetPasswordRequest request,
                                    @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader) {
        if (customerIdHeader == null || customerIdHeader.isEmpty() || "null".equals(customerIdHeader)) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        Long customerId = Long.parseLong(customerIdHeader);
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            return Result.error(401, "账号不存在");
        }
        customer.setPassword(PasswordUtil.hash(request.getPassword()));
        customerMapper.update(customer);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<CustomerVO> me(@RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader) {
        if (customerIdHeader == null || customerIdHeader.isEmpty() || "null".equals(customerIdHeader)) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        Long customerId = Long.parseLong(customerIdHeader);
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            return Result.error(401, "账号不存在");
        }
        return Result.success(toVO(customer));
    }

    private CustomerVO findOrCreateAndBuildVO(String phone) {
        Customer customer = customerMapper.selectByPhone(phone);
        if (customer == null) {
            customer = new Customer();
            customer.setPhone(phone);
            customer.setNickname("顾客" + phone.substring(phone.length() - 4));
            customerMapper.insert(customer);
            customer = customerMapper.selectById(customer.getId());
        }
        return toVO(customer);
    }

    private CustomerVO toVO(Customer customer) {
        CustomerVO vo = new CustomerVO();
        vo.setToken(jwtUtil.generateCustomerToken(customer.getId(), customer.getPhone()));
        vo.setCustomerId(customer.getId());
        vo.setPhone(customer.getPhone());
        vo.setNickname(customer.getNickname());
        vo.setAvatarUrl(customer.getAvatarUrl());
        return vo;
    }
}
