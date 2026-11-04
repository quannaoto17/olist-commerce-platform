package com.ecommerce.sla.tier2_ai.schema;

/**
 * Tier 2 Structured Output Schema populated by Ollama via Spring AI BeanOutputConverter.
 */
public record AiReviewExtractionResult(
        ComplaintCategory complaintCategory,
        BlamedParty blamedParty,
        String evidenceSnippet
) {
    public enum ComplaintCategory {
        LATE_DELIVERY,
        DAMAGED_PACKAGE,
        LOST_ITEM,
        WRONG_OR_MISSING_PRODUCT,
        RUDE_COURIER,
        OTHER
    }

    public enum BlamedParty {
        CARRIER_3PL,
        SELLER_MERCHANT,
        BOTH,
        UNCLEAR
    }
}
