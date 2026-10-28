package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.service.CaptchaService;
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

    /** 获取图形验证码：{ captchaId, image(base64 dataURL) } */
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        String[] c = captchaService.create();
        return Result.success(Map.of("captchaId", c[0], "image", c[1]));
    }
}
