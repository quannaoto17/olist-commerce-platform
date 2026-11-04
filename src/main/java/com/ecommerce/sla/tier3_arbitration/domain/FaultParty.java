package com.ecommerce.sla.tier3_arbitration.domain;

/**
 * Final responsibility attribution after cross-checking Tier 1 SQL metrics and Tier 2 AI context.
 */
public enum FaultParty {
    /** 3PL Carrier breached delivery SLA or damaged/lost parcel in transit. */
    CARRIER_3PL_FAULT,

    /** Merchant handed over parcel late to carrier or packed wrong/defective item. */
    SELLER_MERCHANT_FAULT,

    /** Customer complained about late delivery, but SQL timestamps prove on-time delivery. */
    CUSTOMER_SUBJECTIVE_BIAS,

    /** Normal order or unverified complaint. */
    NO_VIOLATION
}
