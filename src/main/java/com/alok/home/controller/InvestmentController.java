package com.alok.home.controller;

import com.alok.home.commons.annotation.LogExecutionTime;
import com.alok.home.commons.model.Investment;
import com.alok.home.response.GetInvestmentsResponse;
import com.alok.home.response.GetInvestmentsRorMetricsResponse;
import com.alok.home.service.InvestmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/investment")
public class InvestmentController {

    private InvestmentService investmentService;
    private Long cacheControlMaxAge;

    public InvestmentController(
            InvestmentService investmentService,
            @Value("${web.cache-control.max-age}") Long cacheControlMaxAge
    ) {
        this.investmentService = investmentService;
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    @LogExecutionTime
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetInvestmentsResponse> getAllInvestments() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getAllInvestments());
    }

    @LogExecutionTime
    @GetMapping(value = "/return", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetInvestmentsRorMetricsResponse> getInvestmentsRor() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getInvestmentsReturnMetrics());
    }

    @LogExecutionTime
    @GetMapping(value = "/month/{yearMonth}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Investment>> getMonthInvestments(
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    ) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getMonthInvestments(yearMonth));
    }

    @LogExecutionTime
    @GetMapping(value = "/head/{head}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Investment>> getHeadInvestments(
            @PathVariable(value = "head") String head
    ) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getHeadInvestments(head));
    }
}
