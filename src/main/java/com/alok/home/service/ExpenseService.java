package com.alok.home.service;

import com.alok.home.commons.dto.api.response.ExpensesMonthSumResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponseAggByDay;
import com.alok.home.commons.entity.Expense;
import com.alok.home.commons.entity.IExpenseCategoryMonthSum;
import com.alok.home.commons.entity.IExpenseMonthSum;
import com.alok.home.commons.entity.YearMonth;
import com.alok.home.commons.utils.DateUtils;
import com.alok.home.config.CacheConfig;
import com.alok.home.commons.repository.ExpenseRepository;
import com.alok.home.commons.dto.api.response.ExpensesMonthSumByCategoryResponse;
import com.alok.home.stream.CustomCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpenseService {

    private ExpenseRepository expenseRepository;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "#root.methodName")
    public ExpensesResponse getAllExpenses() {
        log.info("All Expenses not available in cache");

        List<Expense> expenses = expenseRepository.findAll();
        Date lastExpenseDate = expenseRepository.findLastTransactionDate()
                .orElse(new Date());
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return ExpensesResponse.builder()
                .expenses(expenses.stream()
                        .map(expense -> ExpensesResponse.Expense.builder()
                                .id(expense.getId())
                                .date(expense.getDate())
                                .head(expense.getHead())
                                .amount(expense.getAmount())
                                .category(expense.getCategory())
                                .comment(expense.getComment())
                                .build())
                        .collect(Collectors.toList()))
                .count(expenses.size())
                .lastTransactionDate(lastExpenseDate)
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #yearMonth, #category }")
    public ExpensesResponse getExpensesForMonth(java.time.YearMonth yearMonth, String category) {
        log.info("Current Month Expenses not available in cache");

        List<Expense> expenses = null;
        if (category == null)
           expenses = expenseRepository.findAllForMonth(yearMonth.getYear(), yearMonth.getMonthValue());
        else
            expenses = expenseRepository.findAllForMonthAndCategory(yearMonth.getYear(), yearMonth.getMonthValue(), category);

        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return ExpensesResponse.builder()
                .expenses(expenses.stream()
                        .map(expense -> ExpensesResponse.Expense.builder()
                                .id(expense.getId())
                                .date(expense.getDate())
                                .head(expense.getHead())
                                .amount(expense.getAmount())
                                .category(expense.getCategory())
                                .comment(expense.getComment())
                                .build())
                        .collect(Collectors.toList()))
                .count(expenses.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #currentDate }")
    public ExpensesResponseAggByDay getCurrentMonthExpensesSumByDay(LocalDate currentDate) {
        log.info("Current Month Expenses Sum By Day not available in cache");

        Date lastExpenseDate = expenseRepository.findLastTransactionDate()
                .orElse(new Date());

        List<Expense> expenses = expenseRepository.findAllForMonth(currentDate.getYear(), currentDate.getMonthValue());
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return ExpensesResponseAggByDay.builder()
                .expenses(expenses.stream().collect(CustomCollectors.toDayExpenseList()))
                .categoryExpenses(expenses.stream().collect(CustomCollectors.toCategoryExpenseList()))
                .lastTransactionDate(lastExpenseDate)
                .count(expenses.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "#root.methodName")
    public ExpensesMonthSumByCategoryResponse getMonthWiseExpenseCategorySum() {
        log.info("Month wise Expenses Category Sum not available in cache");

        List<IExpenseCategoryMonthSum> expenseCategorySums = expenseRepository.findCategorySumGroupByMonth();

        return ExpensesMonthSumByCategoryResponse.builder()
                .expenseCategorySums(expenseCategorySums.stream().map(
                        expMetricSum -> ExpensesMonthSumByCategoryResponse.ExpenseCategoryMonthSum.builder()
                                .year(expMetricSum.getYearx())
                                .month(expMetricSum.getMonthx())
                                .category(expMetricSum.getCategory())
                                .sum(expMetricSum.getSum())
                                .build()
                ).toList())
                .count(expenseCategorySums.size())
                .build();
    }

    public ExpensesMonthSumByCategoryResponse getYearWiseExpenseCategorySum(Integer year) {
        List<IExpenseCategoryMonthSum> expenseCategorySums = expenseRepository.findCategorySumGroupByYear();

        var records = expenseCategorySums.stream().map(
                        expMetricSum -> ExpensesMonthSumByCategoryResponse.ExpenseCategoryMonthSum.builder()
                                .year(expMetricSum.getYearx())
                                .category(expMetricSum.getCategory())
                                .sum(expMetricSum.getSum())
                                .build()
                )
                .filter(record -> (year == null) || year.equals(record.getYear()))
                .toList();

        return ExpensesMonthSumByCategoryResponse.builder()
                .expenseCategorySums(records)
                .count(records.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #category }")
    public ExpensesMonthSumByCategoryResponse getMonthlyExpenseForCategory(String category) {
        log.info("Monthly was expense for category not available in cache");

        List<IExpenseCategoryMonthSum> expenseCategorySums = expenseRepository.findMonthlyExpenseForCategory(category);

        return ExpensesMonthSumByCategoryResponse.builder()
                .expenseCategorySums(
                        expenseCategorySums.stream().map(
                                expMetricSum -> ExpensesMonthSumByCategoryResponse.ExpenseCategoryMonthSum.builder()
                                        .year(expMetricSum.getYearx())
                                        .month(expMetricSum.getMonthx())
                                        .category(expMetricSum.getCategory())
                                        .sum(expMetricSum.getSum())
                                        .build()
                        ).toList()

                )
                .count(expenseCategorySums.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "#root.methodName")
    public ExpensesMonthSumResponse getMonthWiseExpenseSum() {
        log.info("Month wise Expenses Sum not available in cache");

        List<IExpenseMonthSum> expenseSums = expenseRepository.findSumGroupByMonth();

        return ExpensesMonthSumResponse.builder()
                .expenseCategorySums(expenseSums)
                .count(expenseSums.size())
                .build();
    }

    private Date strToDate(String dateStr) {
        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public ExpensesMonthSumResponse getYearWiseExpenseSum() {
        List<IExpenseMonthSum> expenseSums = expenseRepository.findSumGroupByYear();

        return ExpensesMonthSumResponse.builder()
                .expenseCategorySums(expenseSums)
                .count(expenseSums.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #category }")
    public ExpensesResponse getExpensesForCategory(String category) {
        log.info("Expenses for Category not available in cache");

        List<Expense> expenses = expenseRepository.findAllForCategory(category);
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return ExpensesResponse.builder()
                .expenses(expenses.stream()
                        .map(expense -> ExpensesResponse.Expense.builder()
                                .id(expense.getId())
                                .date(expense.getDate())
                                .head(expense.getHead())
                                .amount(expense.getAmount())
                                .category(expense.getCategory())
                                .comment(expense.getComment())
                                .build())
                        .collect(Collectors.toList()))
                .count(expenses.size())
                .build();
    }

    public List<String> getExpenseCategories() {
        return expenseRepository.findDistinctCategories();
    }

    public List<YearMonth> getExpenseMonths() {
        List<YearMonth> yearMonths = expenseRepository.findDistinctYearMonths();
        Collections.sort(yearMonths);

        return yearMonths;
    }

    @Transactional
    public void saveAllExpenses(List<Expense> expenseRecords) {
        log.info("Delete all the expenses first");
        expenseRepository.deleteAll();

        log.info("Save all the expenses");
        expenseRepository.saveAll(expenseRecords);
    }

    @Deprecated
    private List<ExpensesResponseAggByDay.DayExpense> aggregateExpensesByDayX(List<Expense> expenses) {

        Map<String, Double> dayExpensesSum = expenses.stream()
                .collect(Collectors.groupingBy(
                                expense -> df.format(expense.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Expense::getAmount),
                                        DoubleSummaryStatistics::getSum
                                )
                        )
                );

        Map<String, List<ExpensesResponseAggByDay.Expense>> dayExpenses = expenses.stream()
                .collect(Collectors.groupingBy(
                                expense -> df.format(expense.getDate()),
                                Collectors.mapping(
                                        expense -> ExpensesResponseAggByDay.Expense.builder()
                                                .head(expense.getHead())
                                                .comment(expense.getComment())
                                                .amount(expense.getAmount())
                                                .build(),
                                        Collectors.toList())
                        )
                );

        /*
        Map<String, GetExpensesResponseAggByDay.DayExpense> dayExpensesTest = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> df.format(expense.getDate()),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                                DoubleSummaryStatistics dss = list.stream().collect(
                                        Collectors.summarizingDouble(Expense::getAmount)
                                );

                                List<GetExpensesResponseAggByDay.Expense> expenseRecords = list.stream().collect(
                                        Collectors.mapping(expense -> GetExpensesResponseAggByDay.Expense.builder()
                                                .head(expense.getHead())
                                                .comment(expense.getComment())
                                                .amount(expense.getAmount())
                                                .build(),
                                        Collectors.toList()));

                                Optional<Date> date = list.stream().findAny().map(Expense::getDate);

                                return GetExpensesResponseAggByDay.DayExpense.builder()
                                        .date(date.get())
                                        .amount(dss.getSum())
                                        .expenses(expenseRecords)
                                        .build();

                            }
                        )
                    )
                );
        */

        return dayExpensesSum.entrySet().stream()
                .map(entry -> ExpensesResponseAggByDay.DayExpense.builder()
                        .date(DateUtils.strToDate(entry.getKey()))
                        .amount(entry.getValue())
                        .expenses(dayExpenses.get(entry.getKey()))
                        .build()
                )
                .sorted()
                .collect(Collectors.toList());
    }

    @Deprecated
    private List<ExpensesResponseAggByDay.CategoryExpense> aggregateExpensesByCategory(List<Expense> expenses) {

        Map<String, Double> catExpenses = expenses.stream()
                .collect(
                        Collectors.groupingBy(
                                Expense::getCategory,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        catExpList -> catExpList.stream()
                                                .map(Expense::getAmount)
                                                .reduce(0.0, Double::sum)
                                )
                        )

                );

        return catExpenses.entrySet().stream()
                .map(entry -> ExpensesResponseAggByDay.CategoryExpense.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .sorted()
                .collect(Collectors.toList());
    }
}
