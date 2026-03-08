package com.alok.home.controller;

import com.alok.home.commons.entity.Investment;
import com.alok.home.commons.dto.api.response.InvestmentsResponse;
import com.alok.home.commons.dto.api.response.InvestmentsRorMetricsResponse;
import com.alok.home.response.proto.GetInvestmentsResponseOuterClass;
import com.alok.home.response.proto.GetInvestmentsRorMetricsResponseOuterClass;
import com.alok.home.response.proto.GetRawInvestmentsResponseOuterClass;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;


@RestController
@RequestMapping("/investment")
public abstract class InvestmentController {


    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<InvestmentsResponse> getAllInvestments();

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetInvestmentsResponseOuterClass.GetInvestmentsResponse getAllInvestmentsProto() throws IOException;

    @GetMapping(value = "/return", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<InvestmentsRorMetricsResponse> getInvestmentsRor();

    @GetMapping(value = "/return", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetInvestmentsRorMetricsResponseOuterClass.GetInvestmentsRorMetricsResponse getInvestmentsRorProto() throws IOException;

    @GetMapping(value = "/month/{yearMonth}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<Investment>> getMonthInvestments(
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    );

    @GetMapping(value = "/year/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<Investment>> getYearInvestments(
            @PathVariable(value = "year") Integer year
    );

    @GetMapping(value = "/month/{yearMonth}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getMonthInvestmentsProto(
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    ) throws IOException;

    @GetMapping(value = "/head/{head}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<Investment>> getHeadInvestments(
            @PathVariable(value = "head") String head
    );

    @GetMapping(value = "/head/{head}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getHeadInvestmentsProto(
            @PathVariable(value = "head") String head
    ) throws IOException;
}
