package com.alok.home.controller.impl;

import com.alok.home.commons.constant.Account;
import com.alok.home.commons.dto.api.response.OdionAccountTransactionsResponse;
import com.alok.home.commons.dto.api.response.OdionAccountsBalanceResponse;
import com.alok.home.commons.dto.api.response.OdionMonthlyAccountTransactionResponse;
import com.alok.home.commons.dto.api.response.OdionTransactionsResponse;
import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.controller.OdionController;
import com.alok.home.service.OdionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OdionControllerImpl extends OdionController {

    private final OdionService odionService;
    private final Long cacheControlMaxAge;

    public OdionControllerImpl(
            OdionService odionService,
            @Value("${web.cache-control.max-age}") Long cacheControlMaxAge
    ) {
        this.odionService = odionService;
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    @LogExecutionTime
    public ResponseEntity<OdionTransactionsResponse> getAllTransactions() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getAllTransactions());
    }

    @LogExecutionTime
    public ResponseEntity<OdionAccountTransactionsResponse> getAllTransactions(
            Account account
    ) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getAllTransactions(account));
    }

    @LogExecutionTime
    public ResponseEntity<OdionAccountsBalanceResponse> getAllAccountBalance() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getAllAccountBalance());
    }

    @LogExecutionTime
    public ResponseEntity<OdionMonthlyAccountTransactionResponse> getMonthlyAccountTransaction() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(odionService.getMonthlyAccountTransaction());
    }
}
