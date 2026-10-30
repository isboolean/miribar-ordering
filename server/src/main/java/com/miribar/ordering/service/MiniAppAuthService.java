package com.miribar.ordering.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.common.JwtUtil;
import com.miribar.ordering.dto.CustomerVO;
import com.miribar.ordering.entity.Customer;
import com.miribar.ordering.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 微信小程序登录服务
 * 流程：前端 wx.login 取 code -> 本服务调微信 code2Session 换 openid/session_key
 *      -> 按 openid 查 t_customer（复用顾客表，已有 openid 字段），不存在则自动注册
 *      -> 签发与 H5 顾客端一致的 JWT（payload 含 customerId）
 * 安全约定：session_key 只存在服务端内存中（本系统暂不使用其解密能力，不落库不下发前端）
 */
@Service
public class MiniAppAuthService {

    private final CustomerMapper customerMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${wx.miniapp.app-id:}")
    private String appId;

    @Value("${wx.miniapp.app-secret:}")
    private String appSecret;

    /**
     * 开发者模式：true 时跳过微信 jscode2session，用固定本地 openid 联调。
     * 仅限本地联调使用，上线前必须改为 false 并配置正式 AppID/Secret。
     */
    @Value("${wx.miniapp.dev-mode:true}")
    private boolean devMode;

    public MiniAppAuthService(CustomerMapper customerMapper, JwtUtil jwtUtil) {
        this.customerMapper = customerMapper;
        this.jwtUtil = jwtUtil;
    }

    /** code 换取登录态；返回与 H5 顾客登录一致的 CustomerVO（含 token） */
    public CustomerVO wxLogin(String code) {
        String openid;
        if (devMode) {
            // 开发者模式：固定本地身份，避免每次 wx.login 的 code 都新建顾客记录
            openid = "dev_openid_local";
        } else {
            if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()
                    || appId.startsWith("your-")) {
                throw new BusinessException("小程序未配置 wx.miniapp.app-id/app-secret，请在 application.yml 配置正式值");
            }
            openid = code2Session(code);
        }
        Customer customer = customerMapper.selectByOpenid(openid);
        if (customer == null) {
            customer = new Customer();
            customer.setOpenid(openid);
            // 手机号为空时不拼接昵称，留待手机号登录/用户授权后补充
            customer.setNickname("微信顾客");
            customerMapper.insert(customer);
            customer = customerMapper.selectById(customer.getId());
        }
        CustomerVO vo = new CustomerVO();
        vo.setToken(jwtUtil.generateCustomerToken(customer.getId(), customer.getPhone()));
        vo.setCustomerId(customer.getId());
        vo.setPhone(customer.getPhone());
        vo.setNickname(customer.getNickname());
        vo.setAvatarUrl(customer.getAvatarUrl());
        return vo;
    }

    /** 调用微信 jscode2session，仅返回 openid（session_key 不外传） */
    private String code2Session(String code) {
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid="
                    + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                    + "&secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8)
                    + "&js_code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.path("errcode").asInt(0) != 0) {
                // 常见：40029 code 无效、45011 频率限制、40163 code 已使用
                throw new BusinessException("微信登录失败(" + node.path("errcode").asInt() + "): "
                        + node.path("errmsg").asText(""));
            }
            String openid = node.path("openid").asText(null);
            if (openid == null || openid.isBlank()) {
                throw new BusinessException("微信登录失败：未返回 openid");
            }
            return openid;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("微信登录服务暂不可用，请稍后重试");
        }
    }
}
