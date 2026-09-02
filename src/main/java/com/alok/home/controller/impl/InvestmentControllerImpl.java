package com.alok.home.controller.impl;

import com.alok.home.commons.dto.api.response.InvestmentsResponse;
import com.alok.home.commons.dto.api.response.InvestmentsRorMetricsResponse;
import com.alok.home.commons.dto.api.response.RawInvestmentsResponse;
import com.alok.home.commons.entity.Investment;
import com.alok.home.commons.utils.annotation.runtime.LogExecutionTime;
import com.alok.home.controller.InvestmentController;
import com.alok.home.response.proto.GetInvestmentsResponseOuterClass;
import com.alok.home.response.proto.GetInvestmentsRorMetricsResponseOuterClass;
import com.alok.home.response.proto.GetRawInvestmentsResponseOuterClass;
import com.alok.home.service.InvestmentService;
import com.alok.home.utils.ProtobufUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class InvestmentControllerImpl extends InvestmentController {

    private final InvestmentService investmentService;
    private final Long cacheControlMaxAge;

    public InvestmentControllerImpl(
            InvestmentService investmentService,
            @Value("${web.cache-control.max-age}") Long cacheControlMaxAge
    ) {
        this.investmentService = investmentService;
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    @LogExecutionTime
    public ResponseEntity<InvestmentsResponse> getAllInvestments() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getAllInvestments());
    }

    @LogExecutionTime
    public GetInvestmentsResponseOuterClass.GetInvestmentsResponse getAllInvestmentsProto() throws IOException {

        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(investmentService.getAllInvestments()),
                        GetInvestmentsResponseOuterClass.GetInvestmentsResponse.class
                );
    }

    @LogExecutionTime
    public ResponseEntity<InvestmentsRorMetricsResponse> getInvestmentsRor() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getInvestmentsReturnMetrics());
    }

    @LogExecutionTime
    public GetInvestmentsRorMetricsResponseOuterClass.GetInvestmentsRorMetricsResponse getInvestmentsRorProto() throws IOException {
        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(investmentService.getInvestmentsReturnMetrics()),
                GetInvestmentsRorMetricsResponseOuterClass.GetInvestmentsRorMetricsResponse.class
        );
    }

    @LogExecutionTime
    public ResponseEntity<List<Investment>> getMonthInvestments(
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    ) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getMonthInvestments(yearMonth));
    }

    @LogExecutionTime
    public ResponseEntity<List<Investment>> getYearInvestments(
            @PathVariable(value = "year") Integer year
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getYearInvestments(year));
    }

    @LogExecutionTime
    public GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getMonthInvestmentsProto(
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    ) throws IOException {

        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(RawInvestmentsResponse.builder()
                        .investments(investmentService.getMonthInvestments(yearMonth))
                        .build()),
                GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse.class
        );
    }

    @LogExecutionTime
    public ResponseEntity<List<Investment>> getHeadInvestments(
            @PathVariable("head") String head
    ) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getHeadInvestments(head));
    }

    @LogExecutionTime
    public GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getHeadInvestmentsProto(
            @PathVariable(value = "head") String head
    ) throws IOException {

        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(RawInvestmentsResponse.builder()
                        .investments(investmentService.getHeadInvestments(head))
                        .build()),
                GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse.class
        );
    }
}
