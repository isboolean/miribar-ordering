package com.miribar.ordering.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 小程序静默登录请求：wx.login 获取的临时 code */
@Data
public class WxLoginRequest {
    @NotBlank(message = "code 不能为空")
    private String code;
}
