package com.alok.home.controller;

import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.response.GetTaxesResponse;
import com.alok.home.service.TaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/tax")
public class TaxController {

    private TaxService taxService;
    private Long cacheControlMaxAge;

    public TaxController(
            TaxService taxService,
            @Value("${web.cache-control.max-age}") Long cacheControlMaxAge
    ) {
        this.taxService = taxService;
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    @LogExecutionTime
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetTaxesResponse> getAllTaxes() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(taxService.getAllTaxes());
    }
}
