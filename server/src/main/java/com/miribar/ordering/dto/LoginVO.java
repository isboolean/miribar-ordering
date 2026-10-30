package com.miribar.ordering.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

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
    private List<String> permissions;
    private List<Map<String, Object>> menus;
}
