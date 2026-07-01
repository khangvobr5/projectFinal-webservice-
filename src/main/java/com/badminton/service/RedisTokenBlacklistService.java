package com.badminton.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    
    public void addToBlacklist(String token, long remainingTimeMillis) {
        if (remainingTimeMillis > 0) {
            redisTemplate.opsForValue().set(token, "blacklisted", remainingTimeMillis, TimeUnit.MILLISECONDS);
        }
    }

    
    public boolean isBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
