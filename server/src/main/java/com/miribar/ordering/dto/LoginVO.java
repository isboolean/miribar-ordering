package com.miribar.ordering.dto;

import lombok.Data;

/**
 * 登录结果（不含密码）
 */
@Data
public class LoginVO {
    private String token;
    private Long staffId;
    private Long storeId;
    private String username;
    private String realName;
    private Integer role;
}
