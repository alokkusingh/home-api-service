package com.alok.home.response;

import com.alok.home.commons.entity.OdionTransaction;
import lombok.Builder;
import lombok.Data;

import java.time.YearMonth;
import java.util.Map;

@Data
@Builder
public class GetOdionMonthlyAccountTransactionResponse {

    private Map<OdionTransaction.Account, Map<YearMonth, Double>> accountMonthTransaction;
}
