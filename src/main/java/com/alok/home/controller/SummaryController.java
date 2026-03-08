package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.MonthlySummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/summary")
@Tag(name = "Summary", description = "APIs to fetch aggregated summaries")
public abstract class SummaryController {

    @Operation(
            summary = "Get monthly summary",
            description = "Returns aggregated monthly summary data. Accepts optional query parameters to limit the range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MonthlySummaryResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @Parameter(description = "Optional number of recent months to include in the summary", required = false,
                    schema = @Schema(type = "integer", example = "6"))
            @RequestParam(required = false) Integer lastXMonths,
            @Parameter(description = "Optional start month (inclusive) in format YYYY-MM (e.g. 2007-06)", required = false,
                    schema = @Schema(type = "string", example = "2007-06"))
            @RequestParam(required = false, defaultValue = "2007-06") YearMonth sinceMonth
    );
}