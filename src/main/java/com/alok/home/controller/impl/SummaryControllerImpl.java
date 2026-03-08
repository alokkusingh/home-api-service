package com.alok.home.controller.impl;

import com.alok.home.commons.dto.api.response.MonthlySummaryResponse;
import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.controller.SummaryController;
import com.alok.home.service.SummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SummaryControllerImpl extends SummaryController {

    private SummaryService summaryService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    public SummaryControllerImpl(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @LogExecutionTime
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            Integer lastXMonths,
            YearMonth sinceMonth
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(summaryService.getMonthSummary(lastXMonths, sinceMonth));
    }
}
