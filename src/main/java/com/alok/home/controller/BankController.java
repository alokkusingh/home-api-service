package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.SalaryByCompanyResponse;
import com.alok.home.commons.dto.api.response.TransactionResponse;
import com.alok.home.commons.dto.api.response.TransactionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank")
@Tag(name = "Bank", description = "APIs related to bank transactions and salary aggregations")
public abstract class BankController {

    @Operation(
            summary = "Get all transactions",
            description = "Returns all transactions. Optionally filter by `statementFileName`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionsResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<TransactionsResponse> getAllTransactions(
            @Parameter(description = "Optional statement file name to filter transactions", required = false)
            @RequestParam(value = "statementFileName", required = false) String statementFileName
    );

    @CrossOrigin
    @Operation(
            summary = "Get transaction by id",
            description = "Returns a single transaction identified by its id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied")
    })
    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Transaction id", required = true, example = "1")
            @PathVariable(value = "id") Integer id
    );

    @Operation(
            summary = "Get salary by company",
            description = "Returns salary aggregation grouped by company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SalaryByCompanyResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/salary/bycompany", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<SalaryByCompanyResponse> getSalaryByCompany();
}