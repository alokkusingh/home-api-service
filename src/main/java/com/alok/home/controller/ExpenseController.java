package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.ExpensesMonthSumByCategoryResponse;
import com.alok.home.commons.dto.api.response.ExpensesMonthSumResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponseAggByDay;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense")
public abstract class ExpenseController {


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<ExpensesResponse> getExpenses(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String category
    );

    @GetMapping(value = "/categories/names", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<String>> getExpenseCategories();

    @GetMapping(value = "/months", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<List<com.alok.home.commons.entity.YearMonth>> getExpenseMonths();

    @GetMapping(value = "/monthly/categories/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthlyExpenseForCategory(
            @PathVariable(value = "category") String category
    );

    @GetMapping(value = "/sum_by_category_month", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthWiseExpenseCategorySum();


    @GetMapping(value = "/sum_by_category_year", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<ExpensesMonthSumByCategoryResponse> getYearWiseExpenseCategorySum(
            @RequestParam(value = "year", required = false) Integer year
    );

    @GetMapping(value = "/sum_by_month", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<ExpensesMonthSumResponse> getMonthWiseExpenseSum();

    @GetMapping(value = "/sum_by_year", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<ExpensesMonthSumResponse> getYearWiseExpenseSum();

    @GetMapping(value = "/current_month_by_day", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<ExpensesResponseAggByDay> getCurrentMonthExpensesSumByDay();
}
