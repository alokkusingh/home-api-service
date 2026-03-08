package com.alok.home.controller;

import com.alok.home.commons.entity.Investment;
import com.alok.home.commons.dto.api.response.InvestmentsResponse;
import com.alok.home.commons.dto.api.response.InvestmentsRorMetricsResponse;
import com.alok.home.response.proto.GetInvestmentsResponseOuterClass;
import com.alok.home.response.proto.GetInvestmentsRorMetricsResponseOuterClass;
import com.alok.home.response.proto.GetRawInvestmentsResponseOuterClass;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/investment")
@Tag(name = "Investment", description = "APIs to query investments and return metrics")
public abstract class InvestmentController {

    @Operation(
            summary = "Get all investments (JSON)",
            description = "Returns all investments as JSON."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvestmentsResponse.class))
            ),
            @ApiResponse(responseCode = "204", description = "No investments found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<InvestmentsResponse> getAllInvestments();

    @Operation(
            summary = "Get all investments (Protobuf)",
            description = "Returns all investments in Protobuf format."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/x-protobuf",
                            schema = @Schema(implementation = GetInvestmentsResponseOuterClass.GetInvestmentsResponse.class))
            ),
            @ApiResponse(responseCode = "204", description = "No investments found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetInvestmentsResponseOuterClass.GetInvestmentsResponse getAllInvestmentsProto() throws IOException;

    @Operation(
            summary = "Get investments rate-of-return metrics (JSON)",
            description = "Returns investments ROR metrics as JSON."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvestmentsRorMetricsResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/return", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<InvestmentsRorMetricsResponse> getInvestmentsRor();

    @Operation(
            summary = "Get investments rate-of-return metrics (Protobuf)",
            description = "Returns investments ROR metrics in Protobuf format."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/x-protobuf",
                            schema = @Schema(implementation = GetInvestmentsRorMetricsResponseOuterClass.GetInvestmentsRorMetricsResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/return", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetInvestmentsRorMetricsResponseOuterClass.GetInvestmentsRorMetricsResponse getInvestmentsRorProto() throws IOException;

    @Operation(
            summary = "Get investments for a month",
            description = "Returns investments for the specified year-month (format: YYYY-MM)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Investment.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid yearMonth supplied"),
            @ApiResponse(responseCode = "404", description = "No investments found for the month"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/month/{yearMonth}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<Investment>> getMonthInvestments(
            @Parameter(description = "Year and month in format YYYY-MM (e.g. 2024-03)", required = true, example = "2024-03")
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    );

    @Operation(
            summary = "Get investments for a year",
            description = "Returns investments for the specified year."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Investment.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid year supplied"),
            @ApiResponse(responseCode = "404", description = "No investments found for the year"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/year/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<Investment>> getYearInvestments(
            @Parameter(description = "Year to filter investments (e.g. 2024)", required = true, example = "2024")
            @PathVariable(value = "year") Integer year
    );

    @Operation(
            summary = "Get raw investments for a month (Protobuf)",
            description = "Returns raw investments for the specified year-month in Protobuf format."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/x-protobuf",
                            schema = @Schema(implementation = GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid yearMonth supplied"),
            @ApiResponse(responseCode = "404", description = "No investments found for the month"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/month/{yearMonth}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getMonthInvestmentsProto(
            @Parameter(description = "Year and month in format YYYY-MM (e.g. 2024-03)", required = true, example = "2024-03")
            @PathVariable(value = "yearMonth") YearMonth yearMonth
    ) throws IOException;

    @Operation(
            summary = "Get investments by head",
            description = "Returns investments filtered by head name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Investment.class)))
            ),
            @ApiResponse(responseCode = "404", description = "No investments found for the head"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/head/{head}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<Investment>> getHeadInvestments(
            @Parameter(description = "Head name to filter investments", required = true, example = "Equity")
            @PathVariable(value = "head") String head
    );

    @Operation(
            summary = "Get raw investments by head (Protobuf)",
            description = "Returns raw investments for the specified head in Protobuf format."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/x-protobuf",
                            schema = @Schema(implementation = GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "No investments found for the head"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/head/{head}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public abstract GetRawInvestmentsResponseOuterClass.GetRawInvestmentsResponse getHeadInvestmentsProto(
            @Parameter(description = "Head name to filter investments", required = true, example = "Equity")
            @PathVariable(value = "head") String head
    ) throws IOException;
}