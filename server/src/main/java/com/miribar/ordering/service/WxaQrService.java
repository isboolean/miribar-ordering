package com.miribar.ordering.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miribar.ordering.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * 桌台小程序码生成服务（管理端「生成桌码」用）
 *
 * 码内编码规则：scene = qrToken（即 t_dining_table.qr_token）
 * 约束：getUnlimited 的 scene 最长 32 个可见字符，因此要求 qrToken 生成时不超过 32 位；
 *      顾客扫码后由 pages/index/index 解析 scene 并调用 GET /api/table/qr/{qrToken} 还原桌台。
 * 接口：POST https://api.weixin.qq.com/wxa/getwxacodeunlimit（数量无上限，需 access_token）
 * 文档路径：微信官方文档「小程序码 / 获取不限制的小程序码数量」，以官方最新文档为准。
 */
@Service
public class WxaQrService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${wx.miniapp.app-id:}")
    private String appId;

    @Value("${wx.miniapp.app-secret:}")
    private String appSecret;

    /** 缓存的 access_token（单机内存缓存；多实例部署需改用 Redis 等共享存储） */
    private volatile String cachedToken;
    private volatile long tokenExpireAt = 0;

    /**
     * 生成桌台小程序码
     *
     * @param qrToken   桌码 token（<= 32 字符）
     * @param envVersion release-正式 / trial-体验 / develop-开发（按环境传）
     * @return PNG 图片字节数组
     */
    public byte[] generateTableQr(String qrToken, String envVersion) {
        if (qrToken == null || qrToken.isBlank() || qrToken.length() > 32) {
            throw new BusinessException("桌码 token 长度需为 1~32 字符（小程序码 scene 限制）");
        }
        try {
            String body = objectMapper.writeValueAsString(java.util.Map.of(
                    "scene", qrToken,
                    "page", "pages/index/index",
                    "env_version", envVersion == null || envVersion.isBlank() ? "release" : envVersion,
                    "width", 430,
                    "check_path", false
            ));
            HttpRequest request = HttpRequest.newBuilder(
                            URI.create("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + getAccessToken()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = response.body();
            // 失败时微信返回 JSON（Content-Type: application/json），成功返回图片流
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (contentType.contains("application/json")) {
                JsonNode node = objectMapper.readTree(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
                throw new BusinessException("生成小程序码失败(" + node.path("errcode").asInt() + "): "
                        + node.path("errmsg").asText(""));
            }
            return bytes;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("生成小程序码服务暂不可用，请稍后重试");
        }
    }

    /** 获取 access_token（stable_token，有效期内复用） */
    private String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return cachedToken;
        }
        try {
            String body = objectMapper.writeValueAsString(java.util.Map.of(
                    "grant_type", "client_credential",
                    "appid", appId,
                    "secret", appSecret
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.weixin.qq.com/cgi-bin/stable_token"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.path("errcode").asInt(0) != 0) {
                throw new BusinessException("获取 access_token 失败(" + node.path("errcode").asInt() + "): "
                        + node.path("errmsg").asText(""));
            }
            cachedToken = node.path("access_token").asText();
            // 提前 5 分钟过期，避免边界失效
            tokenExpireAt = System.currentTimeMillis() + (node.path("expires_in").asLong(7200) - 300) * 1000L;
            return cachedToken;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("获取微信凭证失败，请检查 wx.miniapp 配置与服务器网络");
        }
    }

    /** 供控制器输出 base64 dataURL */
    public String generateTableQrBase64(String qrToken, String envVersion) {
        byte[] png = generateTableQr(qrToken, envVersion);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }
}
