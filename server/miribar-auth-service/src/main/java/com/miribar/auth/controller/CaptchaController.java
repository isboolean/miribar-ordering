package com.miribar.auth.controller;

import com.miribar.auth.service.CaptchaService;
import com.miribar.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        String[] c = captchaService.create();
        return Result.success(Map.of("captchaId", c[0], "image", c[1]));
    }
}
