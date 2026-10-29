package com.miribar.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务 - 改用 Redis 存储
 */
@Component
public class CaptchaService {

    private static final long TTL_MINUTES = 5;
    private static final String PREFIX = "captcha:";
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 生成验证码，返回 [captchaId, base64图片] */
    public String[] create() {
        StringBuilder code = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(PREFIX + captchaId, code.toString(), TTL_MINUTES, TimeUnit.MINUTES);
        return new String[]{captchaId, drawImage(code.toString())};
    }

    /** 校验（不区分大小写），一次性使用 */
    public boolean verify(String captchaId, String code) {
        if (captchaId == null || code == null || code.isBlank()) {
            return false;
        }
        String key = PREFIX + captchaId;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            return false;
        }
        redisTemplate.delete(key); // 一次性使用
        return stored.equalsIgnoreCase(code.trim());
    }

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private String drawImage(String code) {
        try {
            BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(243, 244, 246));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            for (int i = 0; i < 4; i++) {
                g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200)));
                g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
            }
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 26);
            g.setFont(font);
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(RANDOM.nextInt(120), RANDOM.nextInt(120), RANDOM.nextInt(150) + 30));
                double theta = (RANDOM.nextDouble() - 0.5) * 0.4;
                int x = 14 + i * 26;
                int y = 29;
                g.rotate(theta, x, y);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
                g.rotate(-theta, x, y);
            }
            for (int i = 0; i < 30; i++) {
                g.setColor(new Color(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)));
                g.fillRect(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 1, 1);
            }
            g.dispose();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", bos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("生成图形验证码失败", e);
        }
    }
}
