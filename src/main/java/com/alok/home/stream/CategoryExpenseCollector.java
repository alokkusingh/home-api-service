package com.alok.home.stream;

import com.alok.home.commons.entity.Expense;
import com.alok.home.commons.dto.api.response.ExpensesResponseAggByDay;
import com.alok.home.commons.utils.DateUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class CategoryExpenseCollector implements Collector<Expense, Map<String, ExpensesResponseAggByDay.CategoryExpense>, List<ExpensesResponseAggByDay.CategoryExpense>> {

    @Override
    public Supplier<Map<String, ExpensesResponseAggByDay.CategoryExpense>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<String, ExpensesResponseAggByDay.CategoryExpense>, Expense> accumulator() {
        return (expenseCategoryMap, expense) -> {
            expenseCategoryMap.putIfAbsent(
                    expense.getCategory(),
                    ExpensesResponseAggByDay.CategoryExpense.builder()
                            .expenses(new ArrayList<>())
                            .category(expense.getCategory())
                            .amount(0d)
                            .build()
            );

            ExpensesResponseAggByDay.CategoryExpense categoryExpense = expenseCategoryMap.get(expense.getCategory());
            categoryExpense.getExpenses().add(ExpensesResponseAggByDay.Expense.builder()
                    .date(DateUtils.convertToLocalDateViaInstant(expense.getDate()))
                    .amount(expense.getAmount())
                    .head(expense.getHead())
                    .comment(expense.getComment())
                    .build());
            categoryExpense.setAmount(categoryExpense.getAmount() + expense.getAmount());
        };
    }

    @Override
    public BinaryOperator<Map<String, ExpensesResponseAggByDay.CategoryExpense>> combiner() {
        return null;
    }

    @Override
    public Function<Map<String, ExpensesResponseAggByDay.CategoryExpense>, List<ExpensesResponseAggByDay.CategoryExpense>> finisher() {

        return (categoryExpensesMap) -> new ArrayList<>(categoryExpensesMap.values());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
