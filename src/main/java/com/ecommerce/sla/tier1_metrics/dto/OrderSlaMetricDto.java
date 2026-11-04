package com.ecommerce.sla.tier1_metrics.dto;

import java.math.BigDecimal;

/**
 * Tier 1 Quantitative SLA Calculation result from PostgreSQL for a single order.
 */
public record OrderSlaMetricDto(
        String orderId,
        Integer reviewScore,
        String reviewCommentMessage,
        BigDecimal totalDeliveryDelayDays,
        BigDecimal sellerHandoverDelayDays,
        boolean isSlaBreached,
        boolean isSellerHandoverLate
) {
}
