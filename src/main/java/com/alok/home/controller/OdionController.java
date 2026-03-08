package com.alok.home.controller;

import com.alok.home.commons.constant.Account;
import com.alok.home.commons.dto.api.response.OdionAccountTransactionsResponse;
import com.alok.home.commons.dto.api.response.OdionAccountsBalanceResponse;
import com.alok.home.commons.dto.api.response.OdionMonthlyAccountTransactionResponse;
import com.alok.home.commons.dto.api.response.OdionTransactionsResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/odion")
@Tag(name = "Odion", description = "APIs to query Odion account transactions and balances")
public abstract class OdionController {

    @Operation(
            summary = "Get all Odion transactions",
            description = "Returns all Odion transactions across accounts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OdionTransactionsResponse.class))
            ),
            @ApiResponse(responseCode = "204", description = "No transactions found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionTransactionsResponse> getAllTransactions();

    @Operation(
            summary = "Get transactions for an Odion account",
            description = "Returns transactions for the specified Odion account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OdionAccountTransactionsResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid account supplied"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/transactions/{account}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionAccountTransactionsResponse> getAllTransactions(
            @Parameter(description = "Account identifier (enum value)", required = true, example = "SAVINGS")
            @PathVariable(value = "account") Account account
    );

    @Operation(
            summary = "Get all Odion account balances",
            description = "Returns balances for all Odion accounts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OdionAccountsBalanceResponse.class))
            ),
            @ApiResponse(responseCode = "204", description = "No account balances available"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionAccountsBalanceResponse> getAllAccountBalance();

    @Operation(
            summary = "Get monthly Odion account transactions",
            description = "Returns monthly aggregated transactions for Odion accounts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OdionMonthlyAccountTransactionResponse.class))
            ),
            @ApiResponse(responseCode = "204", description = "No monthly transactions found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/monthly/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionMonthlyAccountTransactionResponse> getMonthlyAccountTransaction();
}