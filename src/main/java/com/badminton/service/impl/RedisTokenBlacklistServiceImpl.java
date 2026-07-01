package com.badminton.service.impl;

import com.badminton.service.RedisTokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenBlacklistServiceImpl implements RedisTokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBlacklistServiceImpl.class);
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToBlacklist(String token, long expirationMillis) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", expirationMillis, TimeUnit.MILLISECONDS);
            log.debug("Token added to blacklist: {}", token.substring(0, Math.min(20, token.length())));
        } catch (Exception e) {
            log.error("Failed to add token to Redis blacklist: {}", e.getMessage());
            throw new RuntimeException("Failed to add token to blacklist", e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check token blacklist status: {}", e.getMessage());
            return false;
        }
    }
}
