package com.alok.home.controller;

import com.alok.home.commons.entity.OdionTransaction;
import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.response.GetOdionAccountTransactionsResponse;
import com.alok.home.response.GetOdionAccountsBalanceResponse;
import com.alok.home.response.GetOdionMonthlyAccountTransactionResponse;
import com.alok.home.response.GetOdionTransactionsResponse;
import com.alok.home.service.OdionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/odion")
public class OdionController {

    private OdionService odionService;
    private Long cacheControlMaxAge;

    public OdionController(
            OdionService odionService,
            @Value("${web.cache-control.max-age}") Long cacheControlMaxAge
    ) {
        this.odionService = odionService;
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    @LogExecutionTime
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetOdionTransactionsResponse> getAllTransactions() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getAllTransactions());
    }

    @LogExecutionTime
    @GetMapping(value = "/transactions/{account}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetOdionAccountTransactionsResponse> getAllTransactions(
            @PathVariable(value = "account") OdionTransaction.Account account
    ) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getAllTransactions(account));
    }

    @LogExecutionTime
    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetOdionAccountsBalanceResponse> getAllAccountBalance() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getAllAccountBalance());
    }

    @LogExecutionTime
    @GetMapping(value = "/monthly/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetOdionMonthlyAccountTransactionResponse> getMonthlyAccountTransaction() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getMonthlyAccountTransaction());
    }
}
