package com.alok.home.response;

import com.alok.home.commons.entity.Investment;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GetRawInvestmentsResponse {
    List<Investment> investments;
}
