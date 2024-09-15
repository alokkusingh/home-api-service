package com.alok.home.response;

import com.alok.home.commons.entity.OdionTransaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GetOdionAccountsBalanceResponse {

    private List<AccountBalance> accountBalances;
    private Map<OdionTransaction.AccountHead, List<AccountBalance>> headAccountBalances;

    @Data
    @Builder
    public static class AccountBalance {
        private OdionTransaction.Account account;
        private Double balance;
    }
}
