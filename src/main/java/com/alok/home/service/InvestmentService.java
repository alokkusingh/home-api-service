package com.alok.home.service;

import com.alok.home.commons.model.Investment;
import com.alok.home.commons.repository.InvestmentRepository;
import com.alok.home.response.GetInvestmentsResponse;
import com.alok.home.response.GetInvestmentsRorMetricsResponse;
import com.alok.home.stream.CustomCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> pfYearlyRor = getRorByYear(investmentRepository.findAllByHead("PF"));
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> npsYearlyRor = getRorByYear(investmentRepository.findAllByHead("NPS"));
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> licYearlyRor = getRorByYear(investmentRepository.findAllByHead("LIC"));
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> shareYearlyRor = getRorByYear(investmentRepository.findAllByHead("SHARE"));

        Map<YearMonth, List<Investment>> allInvestmentByMonth = investmentRepository.findAll().stream()
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
        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> totalYearlyRor = getRorByYear(allAccumulatedInvestmentByMonth);


        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric cr = GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                .metric("Cumulative Return (%)")
                .PF(getCumulativeReturn(pfYearlyRor))
                .NPS(getCumulativeReturn(npsYearlyRor))
                .LIC(getCumulativeReturn(licYearlyRor))
                .SHARE(getCumulativeReturn(shareYearlyRor))
                .total(getCumulativeReturn(totalYearlyRor))
                .build();

        GetInvestmentsRorMetricsResponse.InvestmentsRorMetric ar = GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                .metric("Average Return (%)")
                .PF(getAverageReturn(pfYearlyRor))
                .NPS(getAverageReturn(npsYearlyRor))
                .LIC(getAverageReturn(licYearlyRor))
                .SHARE(getAverageReturn(shareYearlyRor))
                .total(getAverageReturn(totalYearlyRor))
                .build();

        List<GetInvestmentsRorMetricsResponse.InvestmentsRorMetric> yearRorMetrics = new ArrayList<>();
        yearRorMetrics.add(cr);
        yearRorMetrics.add(ar);
        pfYearlyRor.keySet().stream().sorted(Comparator.reverseOrder()).forEach(
                year -> yearRorMetrics.add(GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.builder()
                            .metric("RoR - " + year)
                            .PF(pfYearlyRor.get(year))
                            .NPS(npsYearlyRor.get(year))
                            .LIC(licYearlyRor.get(year))
                            .SHARE(shareYearlyRor.get(year))
                            .total(totalYearlyRor.get(year))
                            .build())

        );

        return GetInvestmentsRorMetricsResponse.builder()
                .investmentsRorMetrics(yearRorMetrics)
                .build();
    }

    private Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> getRorByYear(List<Investment> investments) {
        Map<Short, List<Investment>> investmentByYear = investments.stream()
                .sorted()
                .filter(investment -> investment.getValueAsOnMonth() != null && investment.getValueAsOnMonth() > 0) // start month
                .filter(investment -> YearMonth.of(investment.getYearx(), investment.getMonthx()).isBefore(YearMonth.now())) // end month
                .collect(Collectors.groupingBy(Investment::getYearx));

        List<Short> years = investmentByYear.keySet().stream().sorted().toList();

        Map<Short, GetInvestmentsRorMetricsResponse.InvestmentsRorMetric.InvestmentsReturn> investmentRorByYear = new HashMap<>();
        years.forEach(year -> {
            List<Investment> previousYearInvestments = investmentByYear.get(Integer.valueOf(year-1).shortValue());
            int prevYearClosingValue = 0;
            if (previousYearInvestments != null && !previousYearInvestments.isEmpty()) {
                prevYearClosingValue = previousYearInvestments.get(previousYearInvestments.size() - 1).getValueAsOnMonth();
            }

            investmentRorByYear.put(year, getRorForYear(investmentByYear.get(year), prevYearClosingValue));
        });

        return investmentRorByYear;
    }

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
                .filter(investmentRecord -> investmentRecord.getContribution() != null && investmentRecord.getContribution() > 0)
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
