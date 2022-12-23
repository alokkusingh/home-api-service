package com.alok.home.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Arrays;

//@EnableCaching
//@Configuration
public class CacheConfig {

    public interface CacheName {
        String TRANSACTION = "transaction";
        String EXPENSE = "expense";
        String SUMMARY = "summary";
    }

    //@Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(CacheName.TRANSACTION),
                new ConcurrentMapCache(CacheName.EXPENSE),
                new ConcurrentMapCache(CacheName.SUMMARY)));
        return cacheManager;
    }
}
