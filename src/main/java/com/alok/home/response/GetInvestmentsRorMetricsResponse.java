package com.alok.home.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GetInvestmentsRorMetricsResponse {

    private List<InvestmentsRorMetric> investmentsRorMetrics;

    @Builder
    @Data
    public static class InvestmentsRorMetric {
        private String metric;
        private InvestmentsReturn PF;
        private InvestmentsReturn NPS;
        private InvestmentsReturn LIC;
        private InvestmentsReturn SHARE;
        private InvestmentsReturn total;


        @Builder
        @Data
        public static class InvestmentsReturn {
            private Integer beg;
            private Integer end;
            private Integer inv;
            private Double ror;
        }
    }


}
