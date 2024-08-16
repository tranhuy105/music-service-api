package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService implements CacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final Duration cacheDuration = Duration.ofHours(6);
    private final Duration streamingCacheDuration = Duration.ofDays(1);

    @Override
    public <T> T cacheOrFetch(String cacheKey, Supplier<T> fallback) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        T cachedValue = null;

        try {
            cachedValue = (T) valueOperations.get(cacheKey);
        } catch (Exception e) {
            log.error("Cache retrieval failed, proceed to use fallback supplier: " + e.getMessage());
        }

        if (cachedValue == null) {
            cachedValue = fallback.get();
            if (cachedValue != null) {
                try {
                    valueOperations.set(cacheKey, cachedValue, cacheDuration);
                } catch (Exception e) {
                    log.error("Cache storing failed: " + e.getMessage());
                }
            }
        }

        return cachedValue;
    }

    @Override
    public void executeWithLock(String lockKey, Runnable operation) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            operation.run();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getCacheKey(CachePrefix prefix, Object... parts) {
        StringBuilder key = new StringBuilder(prefix.getPrefix());
        for (Object part : parts) {
            key.append(part.toString()).append(":");
        }
        key.setLength(key.length() - 1);
        return key.toString();
    }

    @Override
    public void evictCache(CachePrefix cachePrefix, Object... parts) {
        try {
            String cacheKeyPattern = getCacheKey(cachePrefix, parts) + "*";
            Set<String> keys = redisTemplate.keys(cacheKeyPattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception exception) {
            log.error("FAIL TO EVICT CACHE FOR "+cachePrefix.name()+". THIS MAY RESULT IN STALE DATA.", exception);
        }
    }

    @Override
    public StreamingSession getStreamingSessionCache(Long userId) {
        String key = getCacheKey(CachePrefix.STREAMING_SESSION, userId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (StreamingSession) valueOperations.get(key);
    }

    @Override
    public void cacheStreamingSession(Long userId, StreamingSession streamingSession) {
        String key = getCacheKey(CachePrefix.STREAMING_SESSION, userId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, streamingSession, streamingCacheDuration);
    }


    @Override
    public void evictAllCache() {
        for (CachePrefix prefix : CachePrefix.values()) {
            evictCache(prefix);
        }
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return this.redisTemplate;
    }
}
