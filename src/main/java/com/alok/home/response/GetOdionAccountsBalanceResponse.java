package com.alok.home.response;

import com.alok.home.commons.model.OdionTransaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetOdionAccountsBalanceResponse {

    private List<AccountBalance> accountBalances;

    @Data
    @Builder
    public static class AccountBalance {
        private OdionTransaction.Account account;
        private Double balance;
    }
}
