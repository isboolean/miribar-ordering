package com.miribar.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "图形验证码不能为空")
    private String captchaId;
    @NotBlank(message = "图形验证码不能为空")
    private String captchaCode;
}
