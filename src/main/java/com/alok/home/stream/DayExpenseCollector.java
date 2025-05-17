package com.alok.home.stream;

import com.alok.home.commons.entity.Expense;
import com.alok.home.commons.dto.api.response.ExpensesResponseAggByDay;
import com.alok.home.commons.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class DayExpenseCollector implements Collector<Expense, Map<LocalDate, ExpensesResponseAggByDay.DayExpense>, List<ExpensesResponseAggByDay.DayExpense>> {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        @Override
        public Supplier<Map<LocalDate, ExpensesResponseAggByDay.DayExpense>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<Map<LocalDate, ExpensesResponseAggByDay.DayExpense>, Expense> accumulator() {
            return (expenseDayMap, expense) -> {
                LocalDate date = DateUtils.convertToLocalDateViaInstant(expense.getDate());
                expenseDayMap.putIfAbsent(
                        date,
                        ExpensesResponseAggByDay.DayExpense.builder()
                                .expenses(new ArrayList<>())
                                .amount(0d)
                                .date(date)
                                .build()
                );

                ExpensesResponseAggByDay.DayExpense dayExpenses = expenseDayMap.get(date);
                dayExpenses.getExpenses().add(ExpensesResponseAggByDay.Expense.builder()
                        .date(date)
                        .amount(expense.getAmount())
                        .head(expense.getHead())
                        .comment(expense.getComment())
                        .build());
                dayExpenses.setAmount(dayExpenses.getAmount() + expense.getAmount());
            };
        }

        @Override
        public BinaryOperator<Map<LocalDate, ExpensesResponseAggByDay.DayExpense>> combiner() {
            return null;
        }

        @Override
        public Function<Map<LocalDate, ExpensesResponseAggByDay.DayExpense>, List<ExpensesResponseAggByDay.DayExpense>> finisher() {

            return dayExpensesMap -> {
                ArrayList<ExpensesResponseAggByDay.DayExpense> dayWiseExpenses = new ArrayList<>(dayExpensesMap.values());
                dayWiseExpenses.sort(Comparator.comparing(ExpensesResponseAggByDay.DayExpense::getDate).reversed());
                return dayWiseExpenses;
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
    }