package com.alok.home.controller;

import com.alok.home.commons.constant.Account;
import com.alok.home.commons.dto.api.response.OdionAccountTransactionsResponse;
import com.alok.home.commons.dto.api.response.OdionAccountsBalanceResponse;
import com.alok.home.commons.dto.api.response.OdionMonthlyAccountTransactionResponse;
import com.alok.home.commons.dto.api.response.OdionTransactionsResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/odion")
public abstract class OdionController {

    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionTransactionsResponse> getAllTransactions();

    @GetMapping(value = "/transactions/{account}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionAccountTransactionsResponse> getAllTransactions(
            @PathVariable(value = "account") Account account
    );

    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionAccountsBalanceResponse> getAllAccountBalance();

    @GetMapping(value = "/monthly/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<OdionMonthlyAccountTransactionResponse> getMonthlyAccountTransaction();
}
