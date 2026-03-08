package com.alok.home.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public abstract class CacheController {

    @GetMapping("/evict-all")
    public abstract ResponseEntity<Resource> evictAllCache();
}
