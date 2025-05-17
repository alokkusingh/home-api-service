package com.alok.home.stream;

import com.alok.home.commons.constant.Account;
import com.alok.home.commons.entity.OdionTransaction;
import org.javatuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class OdionAccountsBalanceCollector implements Collector<OdionTransaction, Pair<Map<Account, Double>, Map<Account, Double>>, Map<Account, Double>> {


    @Override
    public Supplier<Pair<Map<Account, Double>, Map<Account, Double>>> supplier() {
        return () -> new Pair<>(new HashMap<>(), new HashMap<>());
    }

    @Override
    public BiConsumer<Pair<Map<Account, Double>, Map<Account, Double>>, OdionTransaction> accumulator() {
        return (debitCreditBalanceMapPair, transaction) -> {
            Map<Account, Double> debitBalanceMap = debitCreditBalanceMapPair.getValue0();
            Map<Account, Double> creditBalanceMap = debitCreditBalanceMapPair.getValue1();

            //debitBalanceMap.putIfAbsent(transaction.getDebitAccount(), 0.0D);
            //creditBalanceMap.putIfAbsent(transaction.getCreditAccount(), 0.0D);

            debitBalanceMap.compute(transaction.getDebitAccount(),
                    (account, amount) -> amount == null? transaction.getAmount(): amount + transaction.getAmount()
            );
            creditBalanceMap.compute(transaction.getCreditAccount(),
                    (account, amount) -> amount == null? transaction.getAmount(): amount + transaction.getAmount()
            );
        };
    }

    @Override
    public BinaryOperator<Pair<Map<Account, Double>, Map<Account, Double>>> combiner() {
        return null;
    }

    @Override
    public Function<Pair<Map<Account, Double>, Map<Account, Double>>, Map<Account, Double>> finisher() {
        return (debitCreditBalanceMapPair) -> {
            Map<Account, Double> debitBalanceMap = debitCreditBalanceMapPair.getValue0();
            Map<Account, Double> creditBalanceMap = debitCreditBalanceMapPair.getValue1();

            Map<Account, Double> accountBalanceMap = new HashMap<>();
            accountBalanceMap.putAll(debitBalanceMap);

            for (var entry: creditBalanceMap.entrySet()) {
                accountBalanceMap.compute(entry.getKey(), (account, amount) -> amount == null? -entry.getValue(): amount - entry.getValue());
            }

            return accountBalanceMap;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
