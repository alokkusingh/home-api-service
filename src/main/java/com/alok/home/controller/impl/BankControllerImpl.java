package com.alok.home.controller.impl;

import com.alok.home.commons.dto.api.response.SalaryByCompanyResponse;
import com.alok.home.commons.dto.api.response.TransactionResponse;
import com.alok.home.commons.dto.api.response.TransactionsResponse;
import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.controller.BankController;
import com.alok.home.service.BankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BankControllerImpl extends BankController {

    @Autowired
    private BankService bankService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    @LogExecutionTime
    public ResponseEntity<TransactionsResponse> getAllTransactions(
            String statementFileName
    ) {
        TransactionsResponse response;
        if (statementFileName == null) {
            response = bankService.getAllTransactions();
        } else {
            response = bankService.getAllTransactions(statementFileName);
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(response);
    }

    @LogExecutionTime
    public ResponseEntity<TransactionResponse> getTransaction(Integer id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(bankService.getTransaction(id));
    }

    @LogExecutionTime
    public ResponseEntity<SalaryByCompanyResponse> getSalaryByCompany() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(bankService.getSalaryByCompany());
    }
}
