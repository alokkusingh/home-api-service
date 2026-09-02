package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.ExpensesMonthSumByCategoryResponse;
import com.alok.home.commons.dto.api.response.ExpensesMonthSumResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponse;
import com.alok.home.commons.dto.api.response.ExpensesResponseAggByDay;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense")
@Tag(name = "Expense", description = "APIs to query and aggregate expenses")
public abstract class ExpenseController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get expenses",
            description = "Returns expenses optionally filtered by `yearMonth` and/or `category`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpensesResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<ExpensesResponse> getExpenses(
            @Parameter(description = "Optional year and month in format YYYY-MM (e.g. 2024-03)", required = false)
            @RequestParam(required = false) String yearMonth,
            @Parameter(description = "Optional expense category to filter by", required = false)
            @RequestParam(required = false) String category
    );

    @GetMapping(value = "/categories/names", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get expense categories",
            description = "Returns a list of distinct expense category names."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = String.class)))
            ),
            @ApiResponse(responseCode = "204", description = "No categories found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<List<String>> getExpenseCategories();

    @GetMapping(value = "/months", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get expense months",
            description = "Returns available year-month values for which expenses exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = com.alok.home.commons.entity.YearMonth.class)))
            ),
            @ApiResponse(responseCode = "204", description = "No months found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<List<com.alok.home.commons.entity.YearMonth>> getExpenseMonths();

    @GetMapping(value = "/monthly/categories/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get monthly expense for category",
            description = "Returns monthly expense sums for the given category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpensesMonthSumByCategoryResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthlyExpenseForCategory(
            @Parameter(description = "Expense category name", required = true, example = "Groceries")
            @PathVariable(value = "category") String category
    );

    @GetMapping(value = "/sum_by_category_month", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get month-wise expense sum by category",
            description = "Returns month-wise expense sums grouped by category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpensesMonthSumByCategoryResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<ExpensesMonthSumByCategoryResponse> getMonthWiseExpenseCategorySum();

    @GetMapping(value = "/sum_by_category_year", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get year-wise expense sum by category",
            description = "Returns year-wise expense sums grouped by category. Optional `year` query parameter filters the year."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpensesMonthSumByCategoryResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid year supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<ExpensesMonthSumByCategoryResponse> getYearWiseExpenseCategorySum(
            @Parameter(description = "Optional year to filter results (e.g. 2024)", required = false, example = "2024")
            @RequestParam(value = "year", required = false) Integer year
    );

    @GetMapping(value = "/sum_by_month", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get month-wise expense sum",
            description = "Returns aggregated expense sums per month."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpensesMonthSumResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<ExpensesMonthSumResponse> getMonthWiseExpenseSum();

    @GetMapping(value = "/sum_by_year", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get year-wise expense sum",
            description = "Returns aggregated expense sums per year."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpensesMonthSumResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<ExpensesMonthSumResponse> getYearWiseExpenseSum();

    @GetMapping(value = "/current_month_by_day", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get current month expenses aggregated by day",
            description = "Returns daily aggregation for the current month's expenses."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpensesResponseAggByDay.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public abstract ResponseEntity<ExpensesResponseAggByDay> getCurrentMonthExpensesSumByDay();
}