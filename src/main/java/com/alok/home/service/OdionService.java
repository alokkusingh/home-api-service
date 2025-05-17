package com.alok.home.service;

import com.alok.home.commons.constant.Account;
import com.alok.home.commons.constant.AccountHead;
import com.alok.home.commons.entity.OdionTransaction;
import com.alok.home.commons.repository.OdionTransactionRepository;
import com.alok.home.commons.dto.api.response.OdionAccountTransactionsResponse;
import com.alok.home.commons.dto.api.response.OdionAccountsBalanceResponse;
import com.alok.home.commons.dto.api.response.OdionMonthlyAccountTransactionResponse;
import com.alok.home.commons.dto.api.response.OdionTransactionsResponse;
import com.alok.home.stream.CustomCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collector;

@Slf4j
@Service
public class OdionService {

    private final OdionTransactionRepository odionTransactionRepository;

    public OdionService(OdionTransactionRepository odionTransactionRepository) {
        this.odionTransactionRepository = odionTransactionRepository;
    }

    public OdionTransactionsResponse getAllTransactions() {
        log.info("Get all Odion Transaction not in cache");

        return OdionTransactionsResponse.builder()
            .transactions(odionTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAmount() > 0)
                .toList())
            .build();
    }

    public OdionAccountTransactionsResponse getAllTransactions(Account account) {
        log.info("Get all Odion Account Transaction not in cache");


        return OdionAccountTransactionsResponse.builder()
            .transactions(
                odionTransactionRepository.findTransactionsForAccount(account).stream()
                    .collect(Collector.<OdionTransaction, List<OdionAccountTransactionsResponse.AccountTransaction>, List<OdionAccountTransactionsResponse.AccountTransaction>>of(
                        ArrayList::new,
                        (accountTransactions, transaction) -> {
                            accountTransactions.add(
                                OdionAccountTransactionsResponse.AccountTransaction.builder()
                                    .id(transaction.getId())
                                    .date(transaction.getDate())
                                    .particular(
                                            transaction.getParticular() +
                                                    (transaction.getDebitAccount() == account?  " to " + transaction.getCreditAccount().toString() :  " from " + transaction.getDebitAccount().toString())
                                    )
                                    .debit(transaction.getDebitAccount() == account? transaction.getAmount() : 0)
                                    .credit(transaction.getCreditAccount() == account? transaction.getAmount() : 0)
                                    .build()
                            );
                        },
                        (accountTransactionsPart1, accountTransactionsPart2) -> null,
                        accountTransactions -> accountTransactions
                    ))
            )
            .build();
    }

    public OdionAccountsBalanceResponse getAllAccountBalance() {
        log.info("Get all Odion Account balance not in cache");

        List<OdionTransaction> transactions = odionTransactionRepository.findAll().stream()
            .filter(transaction -> transaction.getAmount() > 0)
            .toList();

        Map<Account, Double> accountBalanceMap = transactions.stream().collect(CustomCollectors.toOdionAccountsBalanceCollector());

        List<OdionAccountsBalanceResponse.AccountBalance> accountBalances = new ArrayList<>();
        for (var entry: accountBalanceMap.entrySet()) {
            accountBalances.add(OdionAccountsBalanceResponse.AccountBalance.builder()
                .account(entry.getKey())
                .balance(entry.getValue()).build());
        }

        Map<AccountHead, List<OdionAccountsBalanceResponse.AccountBalance>> headAccountBalances
                = new LinkedHashMap<>();
        Arrays.stream(AccountHead.values())
            .forEach(head -> {
                headAccountBalances.putIfAbsent(head, new ArrayList<>());
                if (head == AccountHead.SAVINGS_BANKS) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == Account.SAVING
                                    || accountBalance.getAccount() == Account.SBI_MAX_GAIN
                                    || accountBalance.getAccount() == Account.BOB_ADVANTAGE)
                            .sorted(Comparator.comparing(OdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
                if (head == AccountHead.ODION) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == Account.ODION
                                    || accountBalance.getAccount() == Account.INTEREST
                                    || accountBalance.getAccount() == Account.MISC)
                            .sorted(Comparator.comparing(OdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
                if (head == AccountHead.ADARSH) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == Account.ADARSH
                                    || accountBalance.getAccount() == Account.INTEREST_ADARSH
                                    || accountBalance.getAccount() == Account.MISC_ADARSH)
                            .sorted(Comparator.comparing(OdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
                if (head == AccountHead.JYOTHI) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == Account.JYOTHI
                                    || accountBalance.getAccount() == Account.INTEREST_JYOTHI
                                    || accountBalance.getAccount() == Account.MISC_JYOTHI)
                            .sorted(Comparator.comparing(OdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
            }
        );

        return OdionAccountsBalanceResponse.builder()
            .accountBalances(accountBalances)
            .headAccountBalances(headAccountBalances)
            .build();
    }

    public OdionMonthlyAccountTransactionResponse getMonthlyAccountTransaction() {
        log.info("Get monthly Odion Account transaction not in cache");

        return OdionMonthlyAccountTransactionResponse.builder()
            .accountMonthTransaction(odionTransactionRepository.findAll().stream()
                .sorted(Comparator.comparing(OdionTransaction::getDate).reversed())
                .filter(transaction -> transaction.getAmount() > 0)
                .collect(CustomCollectors.toOdionAccountsMonthlyTransactionCollector())
            )
            .build();
    }

    @Transactional
    public void saveAllTransactions(List<OdionTransaction> transactions) {
        log.info("Delete all the Odion transactions first");
        odionTransactionRepository.deleteAll();

        log.info("Save all the Odion transactions");
        odionTransactionRepository.saveAll(transactions);
    }
}
