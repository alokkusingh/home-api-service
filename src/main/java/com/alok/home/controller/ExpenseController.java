package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.ExpensesMonthSumByCategoryResponse;
import com.alok.home.commons.dto.api.response.ExpensesMonthSumResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponseAggByDay;
import com.alok.home.commons.utils.annotation.LogExecutionTime;
import com.alok.home.service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/expense")
public class ExpenseController {


    @Autowired
    private ExpenseService expenseService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    @LogExecutionTime
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpensesResponse> getExpenses(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String category
    ) {
        ExpensesResponse expenses = null;
        if (yearMonth == null && category == null)
           expenses = expenseService.getAllExpenses();
        else if (yearMonth == null && category != null)
            expenses = expenseService.getExpensesForCategory(category);
        else if (yearMonth != null) {
             java.time.YearMonth ym = null;
            if (yearMonth.equals("current_month"))
                ym = java.time.YearMonth.now();
            else {
                String ymArr[] = yearMonth.split("-");
                ym = java.time.YearMonth.of(Integer.valueOf(ymArr[0]), Integer.valueOf(ymArr[1]));
            }

            expenses = expenseService.getExpensesForMonth(
                    ym,
                    category
            );
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenses);
    }

    @LogExecutionTime
    @GetMapping(value = "/categories/names", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getExpenseCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getExpenseCategories());
    }

    @LogExecutionTime
    @GetMapping(value = "/months", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<com.alok.home.commons.entity.YearMonth>> getExpenseMonths() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getExpenseMonths());
    }
    @LogExecutionTime
    @GetMapping(value = "/monthly/categories/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthlyExpenseForCategory(
            @PathVariable(value = "category") String category
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getMonthlyExpenseForCategory(category));
    }

    @LogExecutionTime
    @GetMapping(value = "/sum_by_category_month", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthWiseExpenseCategorySum() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getMonthWiseExpenseCategorySum());
    }


    @LogExecutionTime
    @GetMapping(value = "/sum_by_category_year", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpensesMonthSumByCategoryResponse> getYearWiseExpenseCategorySum(
            @RequestParam(value = "year", required = false) Integer year
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getYearWiseExpenseCategorySum(year));
    }

    @LogExecutionTime
    @GetMapping(value = "/sum_by_month", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpensesMonthSumResponse> getMonthWiseExpenseSum() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getMonthWiseExpenseSum());
    }
    @LogExecutionTime
    @GetMapping(value = "/sum_by_year", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpensesMonthSumResponse> getYearWiseExpenseSum() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getYearWiseExpenseSum());
    }

    @LogExecutionTime
    @GetMapping(value = "/current_month_by_day", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpensesResponseAggByDay> getCurrentMonthExpensesSumByDay() {
        LocalDate currentDate = LocalDate.now();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getCurrentMonthExpensesSumByDay(currentDate));
    }
}
