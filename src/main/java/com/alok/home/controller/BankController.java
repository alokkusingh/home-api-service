package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.SalaryByCompanyResponse;
import com.alok.home.commons.dto.api.response.TransactionResponse;
import com.alok.home.commons.dto.api.response.TransactionsResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank")
public abstract class BankController {


    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<TransactionsResponse> getAllTransactions(
            @RequestParam(value = "statementFileName", required = false) String statementFileName
    );

    @CrossOrigin
    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<TransactionResponse> getTransaction(@PathVariable(value = "id") Integer id);

    @GetMapping(value = "/salary/bycompany", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<SalaryByCompanyResponse> getSalaryByCompany();
}
