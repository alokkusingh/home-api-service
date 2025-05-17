package com.alok.home.controller;

import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.commons.dto.api.response.SalaryByCompanyResponse;
import com.alok.home.commons.dto.api.response.TransactionResponse;
import com.alok.home.commons.dto.api.response.TransactionsResponse;
import com.alok.home.service.BankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/bank")
public class BankController {

    @Autowired
    private BankService bankService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    @LogExecutionTime
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionsResponse> getAllTransactions(
            @RequestParam(value = "statementFileName", required = false) String statementFileName
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
    @CrossOrigin
    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable(value = "id") Integer id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(bankService.getTransaction(id));
    }

    @LogExecutionTime
    @GetMapping(value = "/salary/bycompany", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SalaryByCompanyResponse> getSalaryByCompany() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(bankService.getSalaryByCompany());
    }
}
