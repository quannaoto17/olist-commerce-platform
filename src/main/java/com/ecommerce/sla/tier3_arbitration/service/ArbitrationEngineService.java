package com.ecommerce.sla.tier3_arbitration.service;

import com.ecommerce.sla.tier1_metrics.dto.OrderSlaMetricDto;
import com.ecommerce.sla.tier2_ai.schema.AiReviewExtractionResult;
import com.ecommerce.sla.tier2_ai.schema.AiReviewExtractionResult.ComplaintCategory;
import com.ecommerce.sla.tier3_arbitration.domain.FaultParty;
import org.springframework.stereotype.Service;

/**
 * Tier 3 Automated Arbitration Engine:
 * Cross-verifies SQL timestamps (Tier 1) with AI semantic extraction (Tier 2).
 */
@Service
public class ArbitrationEngineService {

    public record ArbitrationDecision(
            FaultParty faultParty,
            boolean penaltyEligible,
            String reason
    ) {}

    public ArbitrationDecision arbitrate(OrderSlaMetricDto metric, AiReviewExtractionResult aiResult) {
        // Case 1: Customer complains about LATE_DELIVERY
        if (aiResult.complaintCategory() == ComplaintCategory.LATE_DELIVERY) {
            if (!metric.isSlaBreached()) {
                return new ArbitrationDecision(
                        FaultParty.CUSTOMER_SUBJECTIVE_BIAS,
                        false,
                        "Customer claimed late delivery, but SQL timestamps confirm delivery on or before SLA estimate."
                );
            }
            if (metric.isSellerHandoverLate()) {
                return new ArbitrationDecision(
                        FaultParty.SELLER_MERCHANT_FAULT,
                        false,
                        "Order missed SLA because Seller handed over package late to 3PL carrier."
                );
            }
            return new ArbitrationDecision(
                    FaultParty.CARRIER_3PL_FAULT,
                    true,
                    "Verified 3PL SLA Breach: Seller handed over on time, 3PL delivered late with customer complaint."
            );
        }

        // Case 2: Physical package damage or rude courier during transit
        if (aiResult.complaintCategory() == ComplaintCategory.DAMAGED_PACKAGE
                || aiResult.complaintCategory() == ComplaintCategory.LOST_ITEM
                || aiResult.complaintCategory() == ComplaintCategory.RUDE_COURIER) {
            return new ArbitrationDecision(
                    FaultParty.CARRIER_3PL_FAULT,
                    true,
                    "3PL Carrier transit/service violation backed by customer evidence snippet."
            );
        }

        // Case 3: Product defect or missing accessories packed by Seller
        if (aiResult.complaintCategory() == ComplaintCategory.WRONG_OR_MISSING_PRODUCT) {
            return new ArbitrationDecision(
                    FaultParty.SELLER_MERCHANT_FAULT,
                    false,
                    "Product/packing issue attributed to Seller; 3PL is not penalized."
            );
        }

        return new ArbitrationDecision(FaultParty.NO_VIOLATION, false, "No actionable SLA dispute identified.");
    }
}
