package com.alok.home.stream;

import com.alok.home.commons.entity.Investment;
import com.alok.home.response.GetInvestmentsResponse;

import java.time.YearMonth;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import lombok.Data;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;

// Assumption the investment is sorted on YearMonth Asc
public class MonthInvestmentCollector implements
        Collector<
                Investment,
                Quintet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>,
                Quintet<Long, Long, Map<String, Long>, Map<String, Long>, List<GetInvestmentsResponse.MonthInvestment>>
                > {
    @Override
    public Supplier<Quintet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>> supplier() {
        return () -> new Quintet<>(new MutableLong(), Pair.with(new MutableYearMonth(), new MutableLong()), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    @Override
    public BiConsumer<Quintet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>, Investment> accumulator() {
        return (investmentSummaryQuintet, investment) -> {
            var yearMonth = YearMonth.of(investment.getYearx(), investment.getMonthx());

            // Aggregate for total summary
            investmentSummaryQuintet.getValue0().add(investment.getContribution());

            // All Investment type as on value to be accumulated
            var lastMonthValue = investmentSummaryQuintet.getValue1();
            if (yearMonth.equals(lastMonthValue.getValue0().getValue())) {
                lastMonthValue.getValue1().add(investment.getValueAsOnMonth());
            } else {
                lastMonthValue.getValue0().setValue(yearMonth);
                lastMonthValue.getValue1().setValue(investment.getValueAsOnMonth());
            }

            // Aggregation investment for each investment type
            var categoryInvestmentMap = investmentSummaryQuintet.getValue2();
            categoryInvestmentMap.compute(investment.getHead(),
                    (head, value) -> value == null? investment.getContribution(): value + investment.getContribution()
            );

            // Aggregation investment value for each investment type
            var categoryInvestmentValueMap = investmentSummaryQuintet.getValue3();
            categoryInvestmentValueMap.compute(investment.getHead(),
                    (head, value) -> Long.valueOf(investment.getValueAsOnMonth())
            );

            // Aggregation fo the month
            // note - each month for each investment type there will one Investment object
            var yearMonthInvestmentMap = investmentSummaryQuintet.getValue4();
            if (!yearMonthInvestmentMap.containsKey(yearMonth)) {
                yearMonthInvestmentMap.put(yearMonth, GetInvestmentsResponse.MonthInvestment.builder()
                                .yearMonth(yearMonth.toString())
                                .investmentAmount(0L)
                                .asOnInvestment(0L)
                                .asOnValue(0L)
                                .investments(new ArrayList<>())
                        .build());
            }
            var monthInvestment = yearMonthInvestmentMap.get(yearMonth);
            monthInvestment.setInvestmentAmount(monthInvestment.getInvestmentAmount() + investment.getContribution());
            monthInvestment.setAsOnInvestment(investmentSummaryQuintet.getValue0().getValue());
            monthInvestment.setAsOnValue(monthInvestment.getAsOnValue() + investment.getValueAsOnMonth());
            monthInvestment.getInvestments().add(GetInvestmentsResponse.MonthInvestment.Investment.builder()
                    .head(investment.getHead())
                    .investmentAmount(investment.getContribution())
                    .asOnValue(investment.getValueAsOnMonth())
                    .asOnInvestment(Math.toIntExact(categoryInvestmentMap.get(investment.getHead())))
                    .build());
        };
    }

    @Override
    public BinaryOperator<Quintet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>,Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>> combiner() {
        return null;
    }

    @Override
    public Function<
            Quintet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>,
            Quintet<Long, Long, Map<String, Long>, Map<String, Long>, List<GetInvestmentsResponse.MonthInvestment>>
            > finisher() {
        return (investmentSummaryQuintet) -> new Quintet<>(
                investmentSummaryQuintet.getValue0().getValue(),
                investmentSummaryQuintet.getValue1().getValue1().getValue(),
                investmentSummaryQuintet.getValue2(),
                investmentSummaryQuintet.getValue3(),
                new ArrayList(investmentSummaryQuintet.getValue4().values())
        );
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}

@Data
class MutableLong {
    private long value;

    public void add(long value) {
        this.value += value;
    }
}


@Data class MutableYearMonth {
    private YearMonth value;

    public MutableYearMonth() {
        value = YearMonth.of(2007, 1);
    }
}
