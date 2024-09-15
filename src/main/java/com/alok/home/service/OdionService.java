package com.alok.home.service;

import com.alok.home.commons.entity.OdionTransaction;
import com.alok.home.commons.repository.OdionTransactionRepository;
import com.alok.home.response.GetOdionAccountTransactionsResponse;
import com.alok.home.response.GetOdionAccountsBalanceResponse;
import com.alok.home.response.GetOdionMonthlyAccountTransactionResponse;
import com.alok.home.response.GetOdionTransactionsResponse;
import com.alok.home.stream.CustomCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OdionService {

    private final OdionTransactionRepository odionTransactionRepository;

    public OdionService(OdionTransactionRepository odionTransactionRepository) {
        this.odionTransactionRepository = odionTransactionRepository;
    }

    public GetOdionTransactionsResponse getAllTransactions() {
        log.info("Get all Odion Transaction not in cache");

        return GetOdionTransactionsResponse.builder()
            .transactions(odionTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAmount() > 0)
                .toList())
            .build();
    }

    public GetOdionAccountTransactionsResponse getAllTransactions(OdionTransaction.Account account) {
        log.info("Get all Odion Account Transaction not in cache");


        return GetOdionAccountTransactionsResponse.builder()
            .transactions(
                odionTransactionRepository.findTransactionsForAccount(account).stream()
                    .collect(Collector.<OdionTransaction, List<GetOdionAccountTransactionsResponse.AccountTransaction>, List<GetOdionAccountTransactionsResponse.AccountTransaction>>of(
                        ArrayList::new,
                        (accountTransactions, transaction) -> {
                            accountTransactions.add(
                                GetOdionAccountTransactionsResponse.AccountTransaction.builder()
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

    public GetOdionAccountsBalanceResponse getAllAccountBalance() {
        log.info("Get all Odion Account balance not in cache");

        List<OdionTransaction> transactions = odionTransactionRepository.findAll().stream()
            .filter(transaction -> transaction.getAmount() > 0)
            .toList();

        Map<OdionTransaction.Account, Double> accountBalanceMap = transactions.stream().collect(CustomCollectors.toOdionAccountsBalanceCollector());

        List<GetOdionAccountsBalanceResponse.AccountBalance> accountBalances = new ArrayList<>();
        for (var entry: accountBalanceMap.entrySet()) {
            accountBalances.add(GetOdionAccountsBalanceResponse.AccountBalance.builder()
                .account(entry.getKey())
                .balance(entry.getValue()).build());
        }

        Map<OdionTransaction.AccountHead, List<GetOdionAccountsBalanceResponse.AccountBalance>> headAccountBalances
                = new LinkedHashMap<>();
        Arrays.stream(OdionTransaction.AccountHead.values())
            .forEach(head -> {
                headAccountBalances.putIfAbsent(head, new ArrayList<>());
                if (head == OdionTransaction.AccountHead.SAVINGS_BANKS) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == OdionTransaction.Account.SAVING
                                    || accountBalance.getAccount() == OdionTransaction.Account.SBI_MAX_GAIN
                                    || accountBalance.getAccount() == OdionTransaction.Account.BOB_ADVANTAGE)
                            .sorted(Comparator.comparing(GetOdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
                if (head == OdionTransaction.AccountHead.ODION) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == OdionTransaction.Account.ODION
                                    || accountBalance.getAccount() == OdionTransaction.Account.INTEREST
                                    || accountBalance.getAccount() == OdionTransaction.Account.MISC)
                            .sorted(Comparator.comparing(GetOdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
                if (head == OdionTransaction.AccountHead.ADARSH) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == OdionTransaction.Account.ADARSH
                                    || accountBalance.getAccount() == OdionTransaction.Account.INTEREST_ADARSH
                                    || accountBalance.getAccount() == OdionTransaction.Account.MISC_ADARSH)
                            .sorted(Comparator.comparing(GetOdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
                if (head == OdionTransaction.AccountHead.JYOTHI) {
                    headAccountBalances.get(head).addAll(
                        accountBalances.stream()
                            .filter(accountBalance -> accountBalance.getAccount() == OdionTransaction.Account.JYOTHI
                                    || accountBalance.getAccount() == OdionTransaction.Account.INTEREST_JYOTHI
                                    || accountBalance.getAccount() == OdionTransaction.Account.MISC_JYOTHI)
                            .sorted(Comparator.comparing(GetOdionAccountsBalanceResponse.AccountBalance::getBalance))
                            .toList()
                    );
                }
            }
        );

        return GetOdionAccountsBalanceResponse.builder()
            .accountBalances(accountBalances)
            .headAccountBalances(headAccountBalances)
            .build();
    }

    public GetOdionMonthlyAccountTransactionResponse getMonthlyAccountTransaction() {
        log.info("Get monthly Odion Account transaction not in cache");

        return GetOdionMonthlyAccountTransactionResponse.builder()
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
