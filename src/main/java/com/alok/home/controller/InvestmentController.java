package com.alok.home.controller;

import com.alok.home.commons.annotation.LogExecutionTime;
import com.alok.home.commons.model.Investment;
import com.alok.home.response.GetInvestmentsResponse;
import com.alok.home.response.GetInvestmentsRorMetricsResponse;
import com.alok.home.response.GetRawInvestmentsResponse;
import com.alok.home.response.proto.GetInvestmentsResponseOuterClass;
import com.alok.home.response.proto.GetInvestmentsRorMetricsResponseOuterClass;
import com.alok.home.response.proto.GetRawInvestmentsResponseOuterClass;
import com.alok.home.service.InvestmentService;
import com.alok.home.utils.ProtobufUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public GetInvestmentsResponseOuterClass.GetInvestmentsResponse getAllInvestmentsProto() throws IOException {

        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(investmentService.getAllInvestments()),
                        GetInvestmentsResponseOuterClass.GetInvestmentsResponse.class
                );
    }


    @LogExecutionTime
    @GetMapping(value = "/return", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetInvestmentsRorMetricsResponse> getInvestmentsRor() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(investmentService.getInvestmentsReturnMetrics());
    }

    @LogExecutionTime
    @GetMapping(value = "/return", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public GetInvestmentsRorMetricsResponseOuterClass.GetInvestmentsRorMetricsResponse getInvestmentsRorProto() throws IOException {
        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(investmentService.getInvestmentsReturnMetrics()),
                GetInvestmentsRorMetricsResponseOuterClass.GetInvestmentsRorMetricsResponse.class
        );
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
    @GetMapping(value = "/month/{yearMonth}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getMonthInvestmentsProto(
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    ) throws IOException {

        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(GetRawInvestmentsResponse.builder()
                        .investments(investmentService.getMonthInvestments(yearMonth))
                        .build()),
                GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse.class
        );
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

    @LogExecutionTime
    @GetMapping(value = "/head/{head}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getHeadInvestmentsProto(
            @PathVariable(value = "head") String head
    ) throws IOException {

        return ProtobufUtil.fromJson(
                new ObjectMapper().writeValueAsString(GetRawInvestmentsResponse.builder()
                        .investments(investmentService.getHeadInvestments(head))
                        .build()),
                GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse.class
        );
    }
}
