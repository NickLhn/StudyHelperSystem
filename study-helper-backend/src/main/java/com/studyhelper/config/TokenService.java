package com.studyhelper.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TokenService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final byte[] secretBytes;
    private final long expirationSeconds;

    public TokenService(
            @Value("${security.token.secret:study-helper-dev-secret-change-before-production}") String secret,
            @Value("${security.token.expiration-hours:24}") long expirationHours) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationHours * 3600;
    }

    public String generateToken(User user) {
        long now = Instant.now().getEpochSecond();

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("uid", user.getId());
        payload.put("role", user.getRole().name());
        payload.put("iat", now);
        payload.put("exp", now + expirationSeconds);

        try {
            String encodedHeader = encode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = encode(objectMapper.writeValueAsBytes(payload));
            String unsignedToken = encodedHeader + "." + encodedPayload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception e) {
            throw new IllegalStateException("生成登录令牌失败", e);
        }
    }

    public TokenPayload parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("令牌为空");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("令牌格式无效");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("令牌签名无效");
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {
            });

            long exp = getLong(payload.get("exp"));
            if (exp <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("令牌已过期");
            }

            Long userId = getLong(payload.get("uid"));
            String username = String.valueOf(payload.get("sub"));
            User.Role role = User.Role.valueOf(String.valueOf(payload.get("role")));

            return new TokenPayload(userId, username, role);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("令牌解析失败", e);
        }
    }

    private long getLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            return encode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("令牌签名失败", e);
        }
    }

    public record TokenPayload(Long userId, String username, User.Role role) {
    }
}
