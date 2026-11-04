package com.ecommerce.sla.tier3_arbitration;

import com.ecommerce.sla.tier1_metrics.dto.OrderSlaMetricDto;
import com.ecommerce.sla.tier2_ai.schema.AiReviewExtractionResult;
import com.ecommerce.sla.tier2_ai.schema.AiReviewExtractionResult.BlamedParty;
import com.ecommerce.sla.tier2_ai.schema.AiReviewExtractionResult.ComplaintCategory;
import com.ecommerce.sla.tier3_arbitration.domain.FaultParty;
import com.ecommerce.sla.tier3_arbitration.service.ArbitrationEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ArbitrationEngineUnitTest {

    private final ArbitrationEngineService engine = new ArbitrationEngineService();

    @Test
    @DisplayName("Should penalize 3PL when customer complains about late delivery and 3PL breached SLA")
    void shouldAttributeTo3PlWhenSlaBreachedByCarrier() {
        OrderSlaMetricDto metric = new OrderSlaMetricDto(
                "ORD-001", 1, "Giao trễ 5 ngày!",
                new BigDecimal("5.00"), new BigDecimal("-1.00"),
                true, false
        );
        AiReviewExtractionResult aiResult = new AiReviewExtractionResult(
                ComplaintCategory.LATE_DELIVERY, BlamedParty.CARRIER_3PL, "Giao trễ 5 ngày!"
        );

        var decision = engine.arbitrate(metric, aiResult);

        assertThat(decision.faultParty()).isEqualTo(FaultParty.CARRIER_3PL_FAULT);
        assertThat(decision.penaltyEligible()).isTrue();
    }

    @Test
    @DisplayName("Should classify as Customer Subjective Bias when customer complains late but delivered before SLA")
    void shouldClassifyAsCustomerBiasWhenDeliveredOnTime() {
        OrderSlaMetricDto metric = new OrderSlaMetricDto(
                "ORD-002", 2, "Shop giao hàng lâu quá",
                new BigDecimal("-2.50"), new BigDecimal("-1.00"),
                false, false
        );
        AiReviewExtractionResult aiResult = new AiReviewExtractionResult(
                ComplaintCategory.LATE_DELIVERY, BlamedParty.CARRIER_3PL, "Shop giao hàng lâu quá"
        );

        var decision = engine.arbitrate(metric, aiResult);

        assertThat(decision.faultParty()).isEqualTo(FaultParty.CUSTOMER_SUBJECTIVE_BIAS);
        assertThat(decision.penaltyEligible()).isFalse();
    }
}
