package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.MonthlySummaryResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/summary")
public abstract class SummaryController {

    @GetMapping(value = "/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam(required = false) Integer lastXMonths,
            @RequestParam(required = false, defaultValue = "2007-06") YearMonth sinceMonth
    );
}
