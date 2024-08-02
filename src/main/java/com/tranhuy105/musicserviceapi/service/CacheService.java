package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Supplier;

public interface CacheService {
    <T> T cacheOrFetch(String cacheKey, Supplier<T> fallback);
    void executeWithLock(String lockKey, Runnable operation);
    String getCacheKey(CachePrefix prefix, Object... parts);
    void evictCache(CachePrefix cachePrefix, Object... parts);
    StreamingSession getStreamingSessionCache(Long userId);
    void cacheStreamingSession(Long userId, StreamingSession streamingSession);
    void evictAllCache();
    RedisTemplate<String, Object> getRedisTemplate();
}
