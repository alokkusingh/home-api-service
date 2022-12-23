package com.alok.home.response;

import com.alok.home.model.OdionTransaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetOdionTransactionsResponse {

    private List<OdionTransaction> transactions;
}
