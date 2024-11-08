package com.alok.home.stream;

public class CustomCollectors {

    private CustomCollectors() {}

    public static CategoryExpenseCollector toCategoryExpenseList() {
        return new CategoryExpenseCollector();
    }

    public static DayExpenseCollector toDayExpenseList() {
        return new DayExpenseCollector();
    }

    public static MonthInvestmentCollector toMonthInvestmentList() {
        return new MonthInvestmentCollector();
    }

    public static OdionAccountsBalanceCollector toOdionAccountsBalanceCollector() {
        return new OdionAccountsBalanceCollector();
    }

    public static OdionAccountsMonthlyTransactionCollector toOdionAccountsMonthlyTransactionCollector() {
        return new OdionAccountsMonthlyTransactionCollector();
    }

}
