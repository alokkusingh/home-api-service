package com.alok.home.controller.impl;

import com.alok.home.commons.dto.api.response.TaxesResponse;
import com.alok.home.commons.utils.annotation.runtime.LogExecutionTime;
import com.alok.home.controller.TaxController;
import com.alok.home.service.TaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TaxControllerImpl extends TaxController {

    private final TaxService taxService;
    private final Long cacheControlMaxAge;

    public TaxControllerImpl(
            TaxService taxService,
            @Value("${web.cache-control.max-age}") Long cacheControlMaxAge
    ) {
        this.taxService = taxService;
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    @LogExecutionTime
    public ResponseEntity<TaxesResponse> getAllTaxes() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(taxService.getAllTaxes());
    }
}
