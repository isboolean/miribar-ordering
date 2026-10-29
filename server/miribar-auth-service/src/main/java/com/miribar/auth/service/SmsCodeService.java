package com.miribar.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务 - 改用 Redis 存储
 */
@Component
public class SmsCodeService {

    private static final String PREFIX = "sms:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    @Value("${sms.dev-mode:true}")
    private boolean devMode;

    public SmsCodeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String sendCode(String phone) {
        String code = devMode ? "1234" : String.format("%06d", RANDOM.nextInt(1_000_000));
        redisTemplate.opsForValue().set(PREFIX + phone, code, 5, TimeUnit.MINUTES);
        if (!devMode) {
            // TODO: 对接短信服务商
        }
        return code;
    }

    public boolean verify(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }
        String stored = redisTemplate.opsForValue().get(PREFIX + phone);
        return stored != null && stored.equals(code);
    }

    public void consume(String phone) {
        redisTemplate.delete(PREFIX + phone);
    }

    public boolean isDevMode() {
        return devMode;
    }
}
