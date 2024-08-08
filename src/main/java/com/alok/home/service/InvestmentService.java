package com.alok.home.service;

import com.alok.home.commons.model.Investment;
import com.alok.home.commons.repository.InvestmentRepository;
import com.alok.home.response.GetInvestmentsResponse;
import com.alok.home.response.GetInvestmentsRorMetricsResponse;
import com.alok.home.stream.CustomCollectors;
import lombok.extern.slf4j.Slf4j;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvestmentService {

    @Autowired
    private InvestmentRepository investmentRepository;

    public GetInvestmentsResponse getAllInvestments() {
        log.info("All Investments not available in cache");

        List<Investment> investments = investmentRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        short currentYear = Short.parseShort(DateTimeFormatter.ofPattern("yyyy").format(now));
        short currentMonth = Short.parseShort(DateTimeFormatter.ofPattern("MM").format(now));

        return investments.stream()
                .filter(investment -> {
                    if (currentYear < investment.getYearx())
                        return false;
                    if (currentYear > investment.getYearx())
                        return true;
                    if (currentMonth < investment.getMonthx())
                        return false;
                    return true;
                })
                .collect(Collectors.collectingAndThen(
                        CustomCollectors.toMonthInvestmentList(),
                        monthInvestmentSummary -> GetInvestmentsResponse.builder()
                                .investmentAmount(monthInvestmentSummary.getValue0())
                                .asOnValue(monthInvestmentSummary.getValue1())
                                .monthInvestments(monthInvestmentSummary.getValue4())
                                .investmentsByType(monthInvestmentSummary.getValue2())
                                .investmentsValueByType(monthInvestmentSummary.getValue3())
                                .build()
                ));
    }

    public GetInvestmentsRorMetricsResponse getInvestmentsReturnMetrics() {

        List<Investment> pfInvestments = investmentRepository.findAllByHead("PF");
        List<Investment> npsInvestments = investmentRepository.findAllByHead("NPS");
        List<Investment> licInvestments = investmentRepository.findAllByHead("LIC");
        List<Investment> shareInvestments = investmentRepository.findAllByHead("SHARE");
        List<Investment> mfInvestments = investmentRepository.findAllByHead("MF");
        List<Investment> allInvestments = investmentRepository.findAll();

        List<Investment> currentMonthInvestments = investmentRepository.findAllByYearMonth(YearMonth.now().getYear(), YearMonth.now().getMonthValue());
        List<Investment> currentMonthMinusOneInvestments = investmentRepository.findAllByYearMonth(YearMonth.now().minusMonths(1).getYear(), YearMonth.now().minusMonths(1).getMonthValue());
        List<Investment> currentMonthMinusTwoInvestments = investmentRepository.findAllByYearMonth(YearMonth.now().minusMonths(2).getYear(), YearMonth.now().minusMonths(2).getMonthValue());

        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> pfYearlyRor = getXirrByYear(pfInvestments);
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> npsYearlyRor = getXirrByYear(npsInvestments);
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> licYearlyRor = getXirrByYear(licInvestments);
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> shareYearlyRor = getXirrByYear(shareInvestments);
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> mfYearlyRor = getXirrByYear(mfInvestments);

        Map<YearMonth, List<Investment>> allInvestmentByMonth = allInvestments.stream()
                .collect(Collectors.groupingBy(investment -> YearMonth.of(investment.getYearx(), investment.getMonthx())));
        List<Investment> allAccumulatedInvestmentByMonth = allInvestmentByMonth.entrySet()
                .stream()
                .map(entry -> entry.getValue().stream().reduce(
                                Investment.builder()
                                        .contribution(0)
                                        .valueAsOnMonth(0)
                                        .head("total")
                                        .yearx((short) entry.getKey().getYear())
                                        .monthx((short) entry.getKey().getMonthValue())
                                        .build(),
                                (accumulatedInv, inv) -> {
                                    accumulatedInv.setContribution(accumulatedInv.getContribution() + inv.getContribution());
                                    accumulatedInv.setValueAsOnMonth(accumulatedInv.getValueAsOnMonth() + inv.getValueAsOnMonth());
                                    return accumulatedInv;
                                }
                        )
                )
                .toList();

        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> totalYearlyRor = getXirrByYear(allInvestments, allAccumulatedInvestmentByMonth);
        //Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> totalYearlyRor = getXirrByYear(allAccumulatedInvestmentByMonth);

        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric cr = GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                .metric("Cumulative Return (%)")
                .PF(getCumulativeReturn(pfYearlyRor))
                .NPS(getCumulativeReturn(npsYearlyRor))
                .LIC(getCumulativeReturn(licYearlyRor))
                .SHARE(getCumulativeReturn(shareYearlyRor))
                .MF(getCumulativeReturn(mfYearlyRor))
                .total(getCumulativeReturn(totalYearlyRor))
                .build();

        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric ar = GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                .metric("Average Return (%)")
                .PF(getAverageReturn(pfYearlyRor))
                .NPS(getAverageReturn(npsYearlyRor))
                .LIC(getAverageReturn(licYearlyRor))
                .SHARE(getAverageReturn(shareYearlyRor))
                .MF(getAverageReturn(mfYearlyRor))
                .total(getAverageReturn(totalYearlyRor))
                .build();

        // TODO: Current Month
        Investment currentMonthPF = currentMonthInvestments.stream().filter(investment -> investment.getHead().equals("PF")).findFirst().get();
        Investment currentMinusOneMonthPF = currentMonthMinusOneInvestments.stream().filter(investment -> investment.getHead().equals("PF")).findFirst().get();
        Investment currentMonthNPS = currentMonthInvestments.stream().filter(investment -> investment.getHead().equals("NPS")).findFirst().get();
        Investment currentMinusOneMonthNPS = currentMonthMinusOneInvestments.stream().filter(investment -> investment.getHead().equals("NPS")).findFirst().get();
        Investment currentMonthLIC = currentMonthInvestments.stream().filter(investment -> investment.getHead().equals("LIC")).findFirst().get();
        Investment currentMinusOneMonthLIC = currentMonthMinusOneInvestments.stream().filter(investment -> investment.getHead().equals("LIC")).findFirst().get();
        Investment currentMonthShare = currentMonthInvestments.stream().filter(investment -> investment.getHead().equals("SHARE")).findFirst().get();
        Investment currentMinusOneMonthShare = currentMonthMinusOneInvestments.stream().filter(investment -> investment.getHead().equals("SHARE")).findFirst().get();
        Investment currentMonthMF = currentMonthInvestments.stream().filter(investment -> investment.getHead().equals("MF")).findFirst().get();
        Investment currentMinusOneMonthMF = currentMonthMinusOneInvestments.stream().filter(investment -> investment.getHead().equals("MF")).findFirst().get();
        Investment currentMonthAll = currentMonthInvestments.stream().reduce(Investment.builder()
                        .contribution(0)
                        .contributionAsOnMonth(0)
                        .valueAsOnMonth(0)
                        .contribution(0)
                .build(), (total, element) -> {
            total.setContribution(total.getContribution() + element.getContribution());
            total.setContributionAsOnMonth(total.getContributionAsOnMonth() + element.getContributionAsOnMonth());
            total.setValueAsOnMonth(total.getValueAsOnMonth() + element.getValueAsOnMonth());
            return total;
        });
        Investment currentMinusOneMonthAll = currentMonthMinusOneInvestments.stream().reduce(Investment.builder()
                .contribution(0)
                .contributionAsOnMonth(0)
                .valueAsOnMonth(0)
                .contribution(0)
                .build(), (total, element) -> {
            total.setContribution(total.getContribution() + element.getContribution());
            total.setContributionAsOnMonth(total.getContributionAsOnMonth() + element.getContributionAsOnMonth());
            total.setValueAsOnMonth(total.getValueAsOnMonth() + element.getValueAsOnMonth());
            return total;
        });
        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric curMonth = GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                .metric("RoR - " + YearMonth.now().getMonth())
                .PF(
                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                                .beg(currentMinusOneMonthPF.getValueAsOnMonth())
                                .inv(currentMonthPF.getContribution())
                                .end(currentMonthPF.getValueAsOnMonth())
                                //.ror(getXirrForAMonth(List.of(currentMonthPF), currentMinusOneMonthPF.getValueAsOnMonth(), currentMonthPF.getValueAsOnMonth()))
                                .ror(getRorForMonth(currentMonthPF, currentMinusOneMonthPF.getValueAsOnMonth()))
                                .build()
                )
                .NPS(
                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusOneMonthNPS.getValueAsOnMonth())
                        .inv(currentMonthNPS.getContribution())
                        .end(currentMonthNPS.getValueAsOnMonth())
                        //.ror(getXirrForAMonth(List.of(currentMonthNPS), currentMinusOneMonthNPS.getValueAsOnMonth(), currentMonthNPS.getValueAsOnMonth()))
                        .ror(getRorForMonth(currentMonthNPS, currentMinusOneMonthNPS.getValueAsOnMonth()))
                        .build())
                .LIC(                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusOneMonthLIC.getValueAsOnMonth())
                        .inv(currentMonthLIC.getContribution())
                        .end(currentMonthLIC.getValueAsOnMonth())
                        //.ror(getXirrForAMonth(List.of(currentMonthLIC), currentMinusOneMonthLIC.getValueAsOnMonth(), currentMonthLIC.getValueAsOnMonth()))
                        .ror(getRorForMonth(currentMonthLIC, currentMinusOneMonthLIC.getValueAsOnMonth()))
                        .build())
                .SHARE(                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusOneMonthShare.getValueAsOnMonth())
                        .inv(currentMonthShare.getContribution())
                        .end(currentMonthShare.getValueAsOnMonth())
                        //.ror(getXirrForAMonth(List.of(currentMonthShare), currentMinusOneMonthShare.getValueAsOnMonth(), currentMonthShare.getValueAsOnMonth()))
                        .ror(getRorForMonth(currentMonthShare, currentMinusOneMonthShare.getValueAsOnMonth()))
                        .build())
                .MF(                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusOneMonthMF.getValueAsOnMonth())
                        .inv(currentMonthMF.getContribution())
                        .end(currentMonthMF.getValueAsOnMonth())
                        //.ror(getXirrForAMonth(List.of(currentMonthMF), currentMinusOneMonthMF.getValueAsOnMonth(), currentMonthMF.getValueAsOnMonth()))
                        .ror(getRorForMonth(currentMonthMF, currentMinusOneMonthMF.getValueAsOnMonth()))
                        .build())
                .total(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusOneMonthAll.getValueAsOnMonth())
                        .inv(currentMonthAll.getContribution())
                        .end(currentMonthAll.getValueAsOnMonth())
                        .ror(getRorForMonth(currentMonthAll, currentMinusOneMonthAll.getValueAsOnMonth()))
                        .build())
                .build();


        // TODO: Last Month
        Investment currentMinusTwoMonthPF = currentMonthMinusTwoInvestments.stream().filter(investment -> investment.getHead().equals("PF")).findFirst().get();
        Investment currentMinusTwoMonthNPS = currentMonthMinusTwoInvestments.stream().filter(investment -> investment.getHead().equals("NPS")).findFirst().get();
        Investment currentMinusTwoMonthLIC = currentMonthMinusTwoInvestments.stream().filter(investment -> investment.getHead().equals("LIC")).findFirst().get();
        Investment currentMinusTwoMonthShare = currentMonthMinusTwoInvestments.stream().filter(investment -> investment.getHead().equals("SHARE")).findFirst().get();
        Investment currentMinusTwoMonthMF = currentMonthMinusTwoInvestments.stream().filter(investment -> investment.getHead().equals("MF")).findFirst().get();
        Investment currentMinusTwoMonthAll = currentMonthMinusTwoInvestments.stream().reduce(Investment.builder()
                .contribution(0)
                .contributionAsOnMonth(0)
                .valueAsOnMonth(0)
                .contribution(0)
                .build(), (total, element) -> {
            total.setContribution(total.getContribution() + element.getContribution());
            total.setContributionAsOnMonth(total.getContributionAsOnMonth() + element.getContributionAsOnMonth());
            total.setValueAsOnMonth(total.getValueAsOnMonth() + element.getValueAsOnMonth());
            return total;
        });
        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric prevMonth = GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                .metric("RoR - " + YearMonth.now().minusMonths(1).getMonth())
                .PF(
                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                                .beg(currentMinusTwoMonthPF.getValueAsOnMonth())
                                .inv(currentMinusOneMonthPF.getContribution())
                                .end(currentMinusOneMonthPF.getValueAsOnMonth())
                                //.ror(getXirrForAMonth(List.of(currentMinusOneMonthPF), currentMinusTwoMonthPF.getValueAsOnMonth(), currentMinusOneMonthPF.getValueAsOnMonth()))
                                .ror(getRorForMonth(currentMinusOneMonthPF, currentMinusTwoMonthPF.getValueAsOnMonth()))
                                .build()
                )
                .NPS(
                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                                .beg(currentMinusTwoMonthNPS.getValueAsOnMonth())
                                .inv(currentMinusOneMonthNPS.getContribution())
                                .end(currentMinusOneMonthNPS.getValueAsOnMonth())
                                //.ror(getXirrForAMonth(List.of(currentMinusOneMonthNPS), currentMinusTwoMonthNPS.getValueAsOnMonth(), currentMinusOneMonthNPS.getValueAsOnMonth()))
                                .ror(getRorForMonth(currentMinusOneMonthNPS, currentMinusTwoMonthNPS.getValueAsOnMonth()))
                                .build())
                .LIC(                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusTwoMonthLIC.getValueAsOnMonth())
                        .inv(currentMinusOneMonthLIC.getContribution())
                        .end(currentMinusOneMonthLIC.getValueAsOnMonth())
                        //.ror(getXirrForAMonth(List.of(currentMinusOneMonthLIC), currentMinusTwoMonthLIC.getValueAsOnMonth(), currentMinusOneMonthLIC.getValueAsOnMonth()))
                        .ror(getRorForMonth(currentMinusOneMonthLIC, currentMinusTwoMonthLIC.getValueAsOnMonth()))
                        .build())
                .SHARE(                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusTwoMonthShare.getValueAsOnMonth())
                        .inv(currentMinusOneMonthShare.getContribution())
                        .end(currentMinusOneMonthShare.getValueAsOnMonth())
                        //.ror(getXirrForAMonth(List.of(currentMinusOneMonthShare), currentMinusTwoMonthShare.getValueAsOnMonth(), currentMinusOneMonthShare.getValueAsOnMonth()))
                        .ror(getRorForMonth(currentMinusOneMonthShare, currentMinusTwoMonthShare.getValueAsOnMonth()))
                        .build())
                .MF(                        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusTwoMonthMF.getValueAsOnMonth())
                        .inv(currentMinusOneMonthMF.getContribution())
                        .end(currentMinusOneMonthMF.getValueAsOnMonth())
                        //.ror(getXirrForAMonth(List.of(currentMinusOneMonthMF), currentMinusTwoMonthMF.getValueAsOnMonth(), currentMinusOneMonthMF.getValueAsOnMonth()))
                        .ror(getRorForMonth(currentMinusOneMonthMF, currentMinusTwoMonthMF.getValueAsOnMonth()))
                        .build())
                .total(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                        .beg(currentMinusTwoMonthAll.getValueAsOnMonth())
                        .inv(currentMinusOneMonthAll.getContribution())
                        .end(currentMinusOneMonthAll.getValueAsOnMonth())
                        .ror(getRorForMonth(currentMinusOneMonthAll, currentMinusTwoMonthAll.getValueAsOnMonth()))
                        .build())
                .build();

        List<GetInvestmentsRorMetricsResponse.InvestmentsRorMetric> yearRorMetrics = new ArrayList<>();
        yearRorMetrics.add(cr);
        yearRorMetrics.add(ar);
        yearRorMetrics.add(curMonth);
        yearRorMetrics.add(prevMonth);
        pfYearlyRor.keySet().stream().sorted(Comparator.reverseOrder()).forEach(
                year -> yearRorMetrics.add(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                            .metric("RoR - " + year)
                            .PF(pfYearlyRor.get(year))
                            .NPS(npsYearlyRor.get(year))
                            .LIC(licYearlyRor.get(year))
                            .SHARE(shareYearlyRor.get(year))
                            .MF(mfYearlyRor.get(year))
                            .total(totalYearlyRor.get(year))
                            .build())

        );

        return GetInvestmentsRorMetricsResponse.builder()
                .investmentsRorMetrics(yearRorMetrics)
                .build();
    }

    private Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> getXirrByYear(List<Investment> investments) {
        Map<Short, List<Investment>> investmentByYear = investments.stream()
                .sorted()
                .filter(investment -> investment.getValueAsOnMonth() != null && investment.getValueAsOnMonth() > 0) // start month
                .filter(investment -> YearMonth.of(investment.getYearx(), investment.getMonthx()).isBefore(YearMonth.now().plusMonths(1))) // end month
                .collect(Collectors.groupingBy(Investment::getYearx));

        List<Short> years = investmentByYear.keySet().stream().sorted().toList();

        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> investmentRorByYear = new HashMap<>();
        years.forEach(year -> {
            List<Investment> previousYearInvestments = investmentByYear.get(Integer.valueOf(year-1).shortValue());
            int prevYearClosingValue = 0;
            if (previousYearInvestments != null && !previousYearInvestments.isEmpty()) {
                prevYearClosingValue = previousYearInvestments.get(previousYearInvestments.size() - 1).getValueAsOnMonth();
            }

            Integer thisYearClosingBalance = investmentByYear.get(year).stream()
                    .map(Investment::getValueAsOnMonth)
                    .toList().get(investmentByYear.get(year).size()-1);

            Integer totalContribution = investmentByYear.get(year).stream()
                    .map(Investment::getContribution)
                    .reduce(0, Integer::sum);

            //investmentRorByYear.put(year, getRorForYear(investmentByYear.get(year), prevYearClosingValue));
            investmentRorByYear.put(year, getInvestmentSummary(investmentByYear.get(year), prevYearClosingValue, thisYearClosingBalance, totalContribution));
        });

        return investmentRorByYear;
    }

    private Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> getXirrByYear(List<Investment> investments, List<Investment> monthlyCumulitivInvestments) {
        Map<Short, List<Investment>> investmentByYear = investments.stream()
                .sorted()
                .filter(investment -> investment.getValueAsOnMonth() != null && investment.getValueAsOnMonth() > 0) // start month
                .filter(investment -> YearMonth.of(investment.getYearx(), investment.getMonthx()).isBefore(YearMonth.now().plusMonths(1))) // end month
                .collect(Collectors.groupingBy(Investment::getYearx));

        Map<Short, List<Investment>> accumulatedInvestmentByYear = monthlyCumulitivInvestments.stream()
                .sorted()
                .filter(investment -> investment.getValueAsOnMonth() != null && investment.getValueAsOnMonth() > 0) // start month
                .filter(investment -> YearMonth.of(investment.getYearx(), investment.getMonthx()).isBefore(YearMonth.now().plusMonths(1))) // end month
                .collect(Collectors.groupingBy(Investment::getYearx));

        List<Short> years = investmentByYear.keySet().stream().sorted().toList();

        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> investmentRorByYear = new HashMap<>();
        years.forEach(year -> {
            List<Investment> previousYearInvestments = accumulatedInvestmentByYear.get(Integer.valueOf(year-1).shortValue());
            int prevYearClosingValue = 0;
            if (previousYearInvestments != null && !previousYearInvestments.isEmpty()) {
                prevYearClosingValue = previousYearInvestments.get(previousYearInvestments.size() - 1).getValueAsOnMonth();
            }

            Integer thisYearClosingBalance = accumulatedInvestmentByYear.get(year).stream()
                    .map(Investment::getValueAsOnMonth)
                    .toList().get(accumulatedInvestmentByYear.get(year).size()-1);

            Integer totalContribution = accumulatedInvestmentByYear.get(year).stream()
                    .map(Investment::getContribution)
                    .reduce(0, Integer::sum);

            //investmentRorByYear.put(year, getRorForYear(investmentByYear.get(year), prevYearClosingValue));
            investmentRorByYear.put(year, getInvestmentSummary(investmentByYear.get(year), prevYearClosingValue, thisYearClosingBalance, totalContribution));
        });

        return investmentRorByYear;
    }


    private GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn getInvestmentSummary(List<Investment> investments, int prevYearClosing, int thisYearClosing, int totalContribution) {

        return GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                .beg(prevYearClosing)
                .inv(totalContribution)
                .end(thisYearClosing)
                .ror(BigDecimal.valueOf(getXirr(investments, prevYearClosing, thisYearClosing)).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build();
    }

    Double getXirr(List<Investment> investments, int openingBalance, int closingBalance) {
        List<org.decampo.xirr.Transaction> transactions = investments.stream()
                .map(investment -> new org.decampo.xirr.Transaction(
                                -investment.getContribution(),
                                String.format("%d-%02d-%02d", investment.getYearx(), investment.getMonthx(), investmentDay(investment.getHead()))
                        )
                )
                .collect(Collectors.toList());

        Short year = investments.stream().findFirst().get().getYearx();

        // add beginning and closing amount of the year as transactions
        String openingDayOfYear = String.format("%d-%02d-%02d", year, 1, 1);
        String closingDayOfYear = year == YearMonth.now().getYear() ?
                String.format("%d-%02d-%02d", YearMonth.now().getYear(), YearMonth.now().getMonthValue(), LocalDate.now().getDayOfMonth()) :
                String.format("%d-%02d-%02d", year, 12, 31);

        transactions.add(new Transaction(-openingBalance, openingDayOfYear));
        transactions.add(new Transaction(closingBalance, closingDayOfYear));

        return Xirr.builder()
                .withTransactions(transactions)
                .build().xirr() * 100;
    }

    Double getXirrForAMonth(List<Investment> investments, int openingBalance, int closingBalance) {
        List<org.decampo.xirr.Transaction> transactions = investments.stream()
                .map(investment -> new org.decampo.xirr.Transaction(
                                -investment.getContribution(),
                                String.format("%d-%02d-%02d", investment.getYearx(), investment.getMonthx(), investmentDay(investment.getHead()))
                        )
                )
                .collect(Collectors.toList());

        Short year = investments.stream().findFirst().get().getYearx();
        Short month = investments.stream().findFirst().get().getMonthx();

        // add beginning and closing amount of the year as transactions
        String openingDayOfYear = String.format("%d-%02d-%02d", year, month, 1);
        String closingDayOfYear = year == YearMonth.now().getYear()  && month == YearMonth.now().getMonthValue()?
                String.format("%d-%02d-%02d", YearMonth.now().getYear(), YearMonth.now().getMonthValue(), LocalDate.now().getDayOfMonth()) :
                String.format("%d-%02d-%02d", year, month, YearMonth.of(year, month).atEndOfMonth().getDayOfMonth());

        transactions.add(new Transaction(-openingBalance, openingDayOfYear));
        transactions.add(new Transaction(closingBalance, closingDayOfYear));

        return Xirr.builder()
                .withTransactions(transactions)
                .build().xirr() * 100 / 12;
    }

    private int investmentDay(String head) {
        return switch (head) {
            case "MF" -> 8;
            case "NPS" -> 14;
            case "PF" -> 15;
            case "SHARE" -> 5;
            case "LIC" -> 15;
            default -> 15;
        };
    }

    @Deprecated
    private GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn getRorForYear(List<Investment> investments, int prevYearClosing) {
        int numberOfMonthInvestments = investments.size();

        Integer totalContribution = investments.stream()
                .map(Investment::getContribution)
                .reduce(0, Integer::sum);

        Integer closingBalance = investments.stream()
                .map(Investment::getValueAsOnMonth)
                .toList().get(investments.size()-1);

        List<Double> monthlyRors = new ArrayList<>(12);
        int prevMonthClosing = prevYearClosing;
        for(Investment investment: investments) {
            monthlyRors.add(getRorForMonth(investment, prevMonthClosing));
            prevMonthClosing = investment.getValueAsOnMonth();
        }

        return GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                .beg(prevYearClosing)
                .inv(totalContribution)
                .end(closingBalance)
                //.ror(new BigDecimal(monthlyRors.stream().mapToDouble(x -> x).average().getAsDouble()).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .ror(BigDecimal.valueOf((monthlyRors.stream().mapToDouble(x -> x).sum())*12/numberOfMonthInvestments).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build();
    }

    private double getRorForMonth(Investment investment, int prevMonthClosing) {
        double totalInvestment = (double) prevMonthClosing + investment.getContribution();
        if (totalInvestment == 0.0)
            return 0.0;

        double gain = investment.getValueAsOnMonth() - totalInvestment;
        return (gain * 100) / totalInvestment;
    }

    private GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn getCumulativeReturn(Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> yeralyRor) {

        return GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                .beg(0)
                .end(yeralyRor.values().stream()
                        .map(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn::getEnd)
                        .reduce(0, Integer::max))
                .inv(yeralyRor.values().stream()
                        .map(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn::getInv)
                        .reduce(0, Integer::sum))
                .ror(BigDecimal.valueOf(
                        yeralyRor.values().stream()
                                .map(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn::getRor)
                                .reduce(0.0, Double::sum)).setScale(2, RoundingMode.HALF_UP).doubleValue()
                )
                .build();
    }

    private GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn getAverageReturn(
            Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> yearlyRors
    ) {

        // this is to handle if the investment started in current year only
        if (yearlyRors.size() == 1) {
            Optional<Short> year = yearlyRors.keySet().stream().findFirst();
            return yearlyRors.get(year.get());
        }
        return GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn.builder()
                .beg(0)
                .end(yearlyRors.values().stream()
                        .map(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn::getEnd)
                        .reduce(0, Integer::max))
                .inv(yearlyRors.values().stream()
                        .map(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn::getInv)
                        .reduce(0, Integer::sum))
                .ror(BigDecimal.valueOf(yearlyRors.values().stream()
                        .filter(investmentsReturn -> investmentsReturn.getBeg() != 0)
                        .map(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn::getRor)
                        .mapToDouble(x -> x)
                        .average()
                        .getAsDouble()).setScale(2, RoundingMode.HALF_UP).doubleValue()
                )
                .build();
    }


    public List<Investment> getMonthInvestments(YearMonth yearMonth) {
        log.info("Month Investments not available in cache");

        return investmentRepository.findAllByYearMonth(yearMonth.getYear(), yearMonth.getMonth().getValue());
    }

    public List<Investment> getHeadInvestments(String head) {

        List<Investment> investments;
        if (head == null || head.equalsIgnoreCase("total")) {
            investments = investmentRepository.findAll();
        } else {
            investments = investmentRepository.findAllByHead(head);
        }

        return investments.stream()
                //.filter(investmentRecord -> investmentRecord.getContribution() != null && investmentRecord.getContribution() > 0)
                .filter(investmentRecord -> investmentRecord.getContribution() != null )
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    /**
     * @deprecated (later release, a batter version of the same function available, kept only for reference)
     */
    @Deprecated(since = "0", forRemoval = true)
    public GetInvestmentsResponse getAllInvestmentsX() {
        log.info("All Investments not available in cache");

        List<Investment> investments = investmentRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        short currentYear = Short.parseShort(DateTimeFormatter.ofPattern("yyyy").format(now));
        short currentMonth = Short.parseShort(DateTimeFormatter.ofPattern("MM").format(now));

        Map<String, GetInvestmentsResponse.MonthInvestment> monthInvestmentsMap = investments.stream()
                .filter(investment -> {
                    if (currentYear < investment.getYearx())
                        return false;
                    if (currentYear > investment.getYearx())
                        return true;
                    if (currentMonth < investment.getMonthx())
                        return false;
                    return true;
                })
                .collect(
                        Collectors.groupingBy(
                                investment -> String.format("%d-%02d", investment.getYearx(), investment.getMonthx()),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream()
                                                .map(investment -> GetInvestmentsResponse.MonthInvestment.Investment.builder()
                                                        .head(investment.getHead())
                                                        .investmentAmount(investment.getContribution())
                                                        .asOnValue(investment.getValueAsOnMonth())
                                                        .build()
                                                )
                                                .collect(
                                                        Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                investmentList -> {
                                                                    Long totalInvestments = investmentList.stream()
                                                                            .map(GetInvestmentsResponse.MonthInvestment.Investment::getInvestmentAmount)
                                                                            .map(Integer::longValue)
                                                                            .reduce(0L, (sum, curr) -> sum + (curr == null?0L:curr));

                                                                    Long totalAsOnValue = investmentList.stream()
                                                                            .map(GetInvestmentsResponse.MonthInvestment.Investment::getAsOnValue)
                                                                            .map(Integer::longValue)
                                                                            .reduce(0L, (sum, curr) -> sum + (curr == null?0L:curr));

                                                                    return GetInvestmentsResponse.MonthInvestment.builder()
                                                                            .investmentAmount(totalInvestments)
                                                                            .asOnValue(totalAsOnValue)
                                                                            .investments(investmentList)
                                                                            .build();
                                                                }

                                                        )
                                                )
                                )
                        )
                );

        AtomicReference<Long> totalInvestments = new AtomicReference<>(0L);
        // TODO this should be the last mon value not the sum
        AtomicReference<Long> totalValues = new AtomicReference<>(0L);
        List<GetInvestmentsResponse.MonthInvestment> monthInvestments = new ArrayList<>(300);
        monthInvestmentsMap.entrySet().forEach(
            entry -> {
                entry.getValue().setYearMonth(entry.getKey());
                totalInvestments.updateAndGet(v -> v + entry.getValue().getInvestmentAmount());
                totalValues.updateAndGet(v -> v + entry.getValue().getAsOnValue());
                monthInvestments.add(entry.getValue());
            }
        );
        Collections.sort(monthInvestments);

        return GetInvestmentsResponse.builder()
                .asOnValue(totalValues.get())
                .monthInvestments(monthInvestments)
                .investmentAmount(totalInvestments.get())
                .build();
    }

    @Transactional
    public void saveAllInvestments(List<Investment> investmentRecords) {
        log.info("Delete all the investments first");
        investmentRepository.deleteAll();

        log.info("Save all the investments");
        investmentRepository.saveAll(investmentRecords);
    }
}
