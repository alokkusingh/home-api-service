package com.alok.home.controller.impl;

import com.alok.home.commons.dto.api.response.ExpensesMonthSumByCategoryResponse;
import com.alok.home.commons.dto.api.response.ExpensesMonthSumResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponseAggByDay;
import com.alok.home.commons.utils.annotation.runtime.LogExecutionTime;
import com.alok.home.controller.ExpenseController;
import com.alok.home.service.ExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ExpenseControllerImpl extends ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    @LogExecutionTime
    public ResponseEntity<ExpensesResponse> getExpenses(
           @RequestParam(value = "yearMonth", required = false) String yearMonth,
           @RequestParam(value = "year", required = false) String year,
           @RequestParam(value = "category", required = false) String category
    ) {
        ExpensesResponse expenses = null;
        if (yearMonth == null && category == null && year == null)
           expenses = expenseService.getAllExpenses();
        else if (yearMonth == null && year == null && category != null)
            expenses = expenseService.getExpensesForCategory(category);
        else if (yearMonth != null) {
           java.time.YearMonth ym;
           if (yearMonth.equals("current_month")) {
                ym = java.time.YearMonth.now();
           } else {
               String[] ymArr = yearMonth.split("-");
               if (ymArr.length != 2) {
                   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "yearMonth must be in YYYY-MM format or 'current_month'");
               }
               try {
                   ym = java.time.YearMonth.of(Integer.parseInt(ymArr[0]), Integer.parseInt(ymArr[1]));
               } catch (NumberFormatException e) {
                   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "yearMonth must be in YYYY-MM format or 'current_month'", e);
               }
           }

           expenses = expenseService.getExpensesForMonth(ym, category);
        }
        else if (year != null) {
            try {
                int yearInt = Integer.parseInt(year);
                expenses = expenseService.getExpensesForYear(yearInt, category);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year must be a valid integer", e);
            }
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenses);
    }

    @LogExecutionTime
    public ResponseEntity<List<String>> getExpenseCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getExpenseCategories());
    }

    @LogExecutionTime
    public ResponseEntity<List<com.alok.home.commons.entity.YearMonth>> getExpenseMonths() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getExpenseMonths());
    }

    @LogExecutionTime
    public ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthlyExpenseForCategory(
            @PathVariable(value = "category") String category
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getMonthlyExpenseForCategory(category));
    }

    @LogExecutionTime
    public ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthWiseExpenseCategorySum(
            @RequestParam(value = "yearMonth", required = false) String yearMonth
    ) {
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                    .body(expenseService.getMonthWiseExpenseCategorySum(yearMonth));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "yearMonth must be in YYYY-MM format or 'current_month'", e);
        }
    }

    @LogExecutionTime
    public ResponseEntity<ExpensesMonthSumByCategoryResponse> getYearWiseExpenseCategorySum(
            @RequestParam(value = "year", required = false) Integer year
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getYearWiseExpenseCategorySum(year));
    }

    @LogExecutionTime
    public ResponseEntity<ExpensesMonthSumResponse> getMonthWiseExpenseSum(
            @RequestParam(value = "year", required = false) Integer year
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getMonthWiseExpenseSum(year));
    }

    @LogExecutionTime
    public ResponseEntity<ExpensesMonthSumResponse> getYearWiseExpenseSum(
            @RequestParam(value = "year", required = false) Integer year
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getYearWiseExpenseSum(year));
    }

    @LogExecutionTime
    public ResponseEntity<ExpensesResponseAggByDay> getCurrentMonthExpensesSumByDay() {
        LocalDate currentDate = LocalDate.now();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getCurrentMonthExpensesSumByDay(currentDate));
    }
}
