package com.miribar.common.dto;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long staffId;
    private Long storeId;
    private String username;
    private String realName;
    private Integer role;
}
