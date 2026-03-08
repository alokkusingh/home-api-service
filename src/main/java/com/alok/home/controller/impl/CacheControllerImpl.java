package com.alok.home.controller.impl;

import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.controller.CacheController;
import com.alok.home.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheControllerImpl extends CacheController {

    private final CacheService cacheService;

    public CacheControllerImpl(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @LogExecutionTime
    public ResponseEntity<Resource> evictAllCache() {
        log.info("Evicting all caches");

        cacheService.evictAllCaches();

        return ResponseEntity.accepted().build();
    }
}
