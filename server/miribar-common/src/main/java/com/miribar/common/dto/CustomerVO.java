package com.miribar.common.dto;

import lombok.Data;

@Data
public class CustomerVO {
    private String token;
    private Long customerId;
    private String phone;
    private String nickname;
    private String avatarUrl;
}
