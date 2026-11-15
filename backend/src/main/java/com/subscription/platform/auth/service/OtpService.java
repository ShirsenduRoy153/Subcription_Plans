package com.subscription.platform.auth.service;

import com.subscription.platform.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String OTP_CODE_PREFIX = "otp:code:";
    private static final String OTP_COOLDOWN_PREFIX = "otp:cooldown:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp:attempts:";

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final int MAX_VERIFY_ATTEMPTS = 3;

    public long getRemainingCooldown(String email) {
        String key = OTP_COOLDOWN_PREFIX + email.toLowerCase().trim();
        Long expireSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return (expireSeconds != null && expireSeconds > 0) ? expireSeconds : 0L;
    }

    public String generateAndStoreOtp(String email) {
        String normalizedEmail = email.toLowerCase().trim();

        // Generate 6-digit cryptographically secure code [100000, 999999]
        int codeInt = 100000 + secureRandom.nextInt(900000);
        String code = String.valueOf(codeInt);

        String codeKey = OTP_CODE_PREFIX + normalizedEmail;
        String cooldownKey = OTP_COOLDOWN_PREFIX + normalizedEmail;
        String attemptsKey = OTP_ATTEMPTS_PREFIX + normalizedEmail;

        // Store OTP with 5m TTL
        redisTemplate.opsForValue().set(codeKey, code, OTP_TTL);

        // Store cooldown with 60s TTL
        redisTemplate.opsForValue().set(cooldownKey, "ACTIVE", COOLDOWN_TTL);

        // Reset any previous attempts
        redisTemplate.delete(attemptsKey);

        log.info("Generated new OTP for email: [masked], TTL: {} seconds", OTP_TTL.toSeconds());
        return code;
    }

    public void verifyOtp(String email, String inputCode) {
        String normalizedEmail = email.toLowerCase().trim();
        String codeKey = OTP_CODE_PREFIX + normalizedEmail;
        String attemptsKey = OTP_ATTEMPTS_PREFIX + normalizedEmail;
        String cooldownKey = OTP_COOLDOWN_PREFIX + normalizedEmail;

        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            throw new BadRequestException("Verification code has expired or was not requested. Please request a new code.");
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, OTP_TTL);
        }

        if (attempts != null && attempts > MAX_VERIFY_ATTEMPTS) {
            // Invalidate the OTP to stop further brute-force attempts
            redisTemplate.delete(codeKey);
            redisTemplate.delete(attemptsKey);
            throw new BadRequestException("Maximum verification attempts exceeded. For your security, this code has been invalidated. Please request a new code.");
        }

        if (!storedCode.equals(inputCode.trim())) {
            long remaining = Math.max(0, MAX_VERIFY_ATTEMPTS - (attempts != null ? attempts : 1));
            throw new BadRequestException("Invalid verification code. " + remaining + " attempt(s) remaining.");
        }

        // On successful match, clean up Redis keys
        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(cooldownKey);
        log.info("OTP verified successfully for email: [masked]");
    }
}

