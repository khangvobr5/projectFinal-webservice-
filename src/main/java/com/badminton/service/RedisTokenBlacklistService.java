package com.badminton.service;

public interface RedisTokenBlacklistService {
    void addToBlacklist(String token, long expirationMillis);
    boolean isBlacklisted(String token);
}
