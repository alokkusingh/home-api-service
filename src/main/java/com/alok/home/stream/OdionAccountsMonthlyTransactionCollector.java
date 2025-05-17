package com.alok.home.stream;


import com.alok.home.commons.constant.Account;
import com.alok.home.commons.entity.OdionTransaction;

import java.time.YearMonth;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class OdionAccountsMonthlyTransactionCollector implements Collector<OdionTransaction, Map<Account, Map<YearMonth, Double>>, Map<Account, Map<YearMonth, Double>>> {


    @Override
    public Supplier<Map<Account, Map<YearMonth, Double>>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<Account, Map<YearMonth, Double>>, OdionTransaction> accumulator() {
        return (monthAccountTransactionMap, transaction) -> {
            monthAccountTransactionMap.putIfAbsent(transaction.getDebitAccount(), new LinkedHashMap<>());
            monthAccountTransactionMap.putIfAbsent(transaction.getCreditAccount(), new LinkedHashMap<>());

            monthAccountTransactionMap.get(transaction.getCreditAccount()).compute(
                    YearMonth.of(transaction.getDate().minusDays(1).getYear(), transaction.getDate().minusDays(1).getMonth()),
                    (key, val) -> val == null? transaction.getAmount(): val + transaction.getAmount()
            );

            monthAccountTransactionMap.get(transaction.getDebitAccount()).compute(
                    YearMonth.of(transaction.getDate().minusDays(1).getYear(), transaction.getDate().minusDays(1).getMonth()),
                    (key, val) -> val == null? -transaction.getAmount(): val - transaction.getAmount()
            );
        };
    }

    @Override
    public BinaryOperator<Map<Account, Map<YearMonth, Double>>> combiner() {
        return null;
    }

    @Override
    public Function<Map<Account, Map<YearMonth, Double>>, Map<Account, Map<YearMonth, Double>>> finisher() {
        return monthAccountTransactionMap -> monthAccountTransactionMap;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
