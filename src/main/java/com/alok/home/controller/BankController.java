package com.alok.home.controller;

import com.alok.home.commons.annotation.LogExecutionTime;
import com.alok.home.response.GetSalaryByCompanyResponse;
import com.alok.home.response.GetTransactionResponse;
import com.alok.home.response.GetTransactionsResponse;
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
    public ResponseEntity<GetTransactionsResponse> getAllTransactions() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(bankService.getAllTransactions());
    }

    @LogExecutionTime
    @CrossOrigin
    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetTransactionResponse> getTransaction(@PathVariable(value = "id") Integer id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(bankService.getTransaction(id));
    }

    @LogExecutionTime
    @GetMapping(value = "/salary/bycompany", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetSalaryByCompanyResponse> getSalaryByCompany() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(bankService.getSalaryByCompany());
    }
}
