package com.miribar.ordering.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务（内存存储）
 * 开发模式（sms.dev-mode=true，默认）：验证码固定为 1234，发送接口直接返回验证码便于联调
 * 生产模式：TODO 对接短信服务商发送，验证码只在内存中保存 5 分钟
 */
@Component
public class SmsCodeService {

    private static final long TTL_MS = 5 * 60 * 1000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    @Value("${sms.dev-mode:true}")
    private boolean devMode;

    private record CodeEntry(String code, long expireAt) {
    }

    /** 发送验证码，开发模式下返回验证码明文（供前端展示/联调） */
    public String sendCode(String phone) {
        String code = devMode ? "1234" : String.format("%06d", RANDOM.nextInt(1_000_000));
        store.put(phone, new CodeEntry(code, System.currentTimeMillis() + TTL_MS));
        if (!devMode) {
            // TODO: 对接短信服务商，发送 code 到 phone
        }
        return code;
    }

    public boolean verify(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }
        CodeEntry entry = store.get(phone);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireAt()) {
            store.remove(phone);
            return false;
        }
        return entry.code().equals(code);
    }

    /** 验证成功后调用，防止重复使用 */
    public void consume(String phone) {
        store.remove(phone);
    }

    public boolean isDevMode() {
        return devMode;
    }
}
