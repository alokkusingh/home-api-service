package com.alok.home.service;

import com.alok.home.commons.constant.InvestmentType;
import com.alok.home.commons.entity.IExpenseMonthSum;
import com.alok.home.commons.entity.Investment;
import com.alok.home.commons.entity.Transaction;
import com.alok.home.commons.entity.TaxMonthly;
import com.alok.home.commons.repository.TaxMonthlyRepository;
import com.alok.home.config.CacheConfig;
import com.alok.home.commons.repository.ExpenseRepository;
import com.alok.home.commons.repository.InvestmentRepository;
import com.alok.home.commons.repository.TransactionRepository;
import com.alok.home.commons.dto.api.response.MonthlySummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SummaryService {

    private ExpenseRepository expenseRepository;
    private TransactionRepository transactionRepository;
    private InvestmentRepository investmentRepository;
    private TaxMonthlyRepository taxMonthlyRepository;

    public SummaryService(ExpenseRepository expenseRepository, TransactionRepository transactionRepository, InvestmentRepository investmentRepository, TaxMonthlyRepository taxMonthlyRepository) {
        this.expenseRepository = expenseRepository;
        this.transactionRepository = transactionRepository;
        this.investmentRepository = investmentRepository;
        this.taxMonthlyRepository = taxMonthlyRepository;
    }

    @Cacheable(CacheConfig.CacheName.SUMMARY)
    public MonthlySummaryResponse getMonthSummary(Integer lastXMonths, YearMonth sinceMonth) {

        log.info("Summary not available in cache");
        List<IExpenseMonthSum> expenseSums = expenseRepository.findSumGroupByMonth();
        List<Transaction> transactions = transactionRepository.findAll();
        List<Investment> investments = investmentRepository.findAll();

        // for company investment the amount is collected in previous month
        // but the sheet contains month as on investment done
        // so, invest perspective month should be current month
        // but salary perspective month should be previous month
        investments.stream()
                .filter(investment -> !(
                        InvestmentType.LIC.name().equals(investment.getHead()) ||
                            InvestmentType.SHARE.name().equals(investment.getHead()) ||
                            InvestmentType.MF.name().equals(investment.getHead())
                    )
                )
                .forEach(investment -> {
                    if (investment.getMonthx() == 1) {
                        investment.setYearx((short) (investment.getYearx() - 1));
                    }
                    if (investment.getMonthx() == 1) {
                        investment.setMonthx((short) 12);
                    } else {
                        investment.setMonthx((short) (investment.getMonthx() - 1));
                    }
                });

        var taxes = taxMonthlyRepository.findAll();

        // From June 2007 June to May 2019 don't have expense entry - so lets add 0 every month later the
        // same will be overridden with actual value
        // This is needed because "expenseSums" collection is used to travers months
        TreeMap<Integer, IExpenseMonthSum> expenseMonthSumMap = new TreeMap<>(Comparator.reverseOrder());

        for (int year = 2007; year <= 2019; year++) {
            for (int month = 1; month <= 12; month++) {
                int finalYear = year;
                int finalMonth = month;
                expenseMonthSumMap.put(
                    Integer.valueOf(String.format("%d%02d", year, month)) ,
                    new IExpenseMonthSum() {
                        @Override
                        public Integer getYearx() {
                            return finalYear;
                        }

                        @Override
                        public Integer getMonthx() {
                            return finalMonth;
                        }

                        @Override
                        public Double getSum() {
                            return 0.0;
                        }
                    }
                );
            }
        }
        for (IExpenseMonthSum expenseMonthSum: expenseSums) {
            expenseMonthSumMap.put(
                    Integer.valueOf(String.format("%d%02d", expenseMonthSum.getYearx(), expenseMonthSum.getMonthx())),
                    expenseMonthSum
            );
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");

        // Salary aggregation
        Map<String, Double> monthlySalary = transactions.stream()
                .filter(Transaction::isSalary)
                .collect(
                        Collectors.groupingBy(
                                transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getDebit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Family transfer aggregation
        Map<String, Double> familyTransferMonthly = transactions.stream()
                .filter(transaction -> "Family".equals(transaction.getHead()) && transaction.getCredit() != null)
                .collect(
                        Collectors.groupingBy(
                                transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getCredit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Family received aggregation
        Map<String, Double> familyReceivedMonthly = transactions.stream()
                .filter(transaction -> "Family".equals(transaction.getHead()) && transaction.getDebit() != null)
                .collect(
                        Collectors.groupingBy(
                                transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getDebit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Investment aggregation
        Map<String, Long> investmentMonthly = investments.stream()
                .collect(
                        Collectors.groupingBy(
                                investment -> String.format("%d-%02d", investment.getYearx(), investment.getMonthx()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingInt(Investment::getContribution),
                                        iss -> iss.getSum()
                                )
                        )
                );

        // Investment through Company aggregation
        Map<String, Long> investmentByCompanyMonthly = investments.stream()
                .filter(investment -> !(
                        InvestmentType.LIC.name().equals(investment.getHead()) ||
                                InvestmentType.SHARE.name().equals(investment.getHead()) ||
                                InvestmentType.MF.name().equals(investment.getHead())
                        )
                )
                .collect(
                        Collectors.groupingBy(
                                investment -> String.format("%d-%02d", investment.getYearx(), investment.getMonthx()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingInt(Investment::getContribution),
                                        iss -> iss.getSum()
                                )
                        )
                );

        // Tax aggregation
        Map<String, Long> taxMonthly = taxes.stream()
                .collect(
                        Collectors.groupingBy(
                                tax -> String.format("%d-%02d", tax.getYearx(), tax.getMonthx()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingInt(TaxMonthly::getPaidAmount),
                                        iss -> iss.getSum()
                                )
                        )
                );

        // Prepare Summary
        YearMonth xMonthBeforeYearMonth = sinceMonth.minusMonths(1);
        if (lastXMonths != null)
            xMonthBeforeYearMonth = YearMonth.now().minusMonths(lastXMonths);

        YearMonth finalXMonthBeforeYearMonth = xMonthBeforeYearMonth;
        List<MonthlySummaryResponse.MonthlySummary> monthSummaryRecord = expenseMonthSumMap.values().stream()
                .filter(expenseMonthRecord -> YearMonth.of(expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()).isAfter(finalXMonthBeforeYearMonth))
                .map(
                        expenseMonthRecord -> MonthlySummaryResponse.MonthlySummary.builder()
                                .year(expenseMonthRecord.getYearx())
                                .month(expenseMonthRecord.getMonthx())
                                .expenseAmount(expenseMonthRecord.getSum())
                                .incomeAmount(
                                        monthlySalary.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx())))
                                .transferAmount(
                                        subtract(
                                                familyTransferMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx())),
                                                familyReceivedMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))
                                        )
                                )
                                .investmentAmount(
                                        investmentMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))
                                )
                                .investmentByCompany(
                                        investmentByCompanyMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))
                                )
                                .taxAmount(
                                        taxMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))
                                )
                                .ctc(
                                        add(investmentByCompanyMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx())),
                                            add(
                                                round(monthlySalary.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))),
                                                taxMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))
                                            )
                                        )
                                )
                                .build()
                )
                .collect(Collectors.toList());


        return MonthlySummaryResponse.builder()
                .records(monthSummaryRecord)
                .count(monthSummaryRecord.size())
                .build();
    }

    private Double subtract(Double a, Double b) {
        if (a == null)
            return b;

        if (b == null)
            return a;

        return a - b;
    }

    private Long add(Long a, Long b) {
        if (a== null && b == null)
            return 0L;

        if (a == null)
            return b;

        if (b == null)
            return a;

        return a + b;
    }

    private Long round(Double x) {
        if (x == null)
            return 0L;

        return Math.round(x);
    }
}
