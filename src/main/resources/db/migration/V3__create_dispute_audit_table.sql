-- ============================================================================
-- V3: Tier 2 (AI Extraction) & Tier 3 (Arbitration Audit Trail) Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS sla_dispute_audits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id VARCHAR(64) NOT NULL UNIQUE REFERENCES olist_orders(order_id),
    review_score INT,
    total_delivery_delay_days NUMERIC(8, 2),
    seller_handover_delay_days NUMERIC(8, 2),
    is_sla_breached BOOLEAN NOT NULL,

    -- Tier 2 AI Extracted Structured Fields
    ai_complaint_category VARCHAR(64),
    ai_blamed_party VARCHAR(32),
    ai_evidence_snippet TEXT,
    ai_raw_payload JSONB,

    -- Tier 3 Final Arbitration Result
    final_fault_party VARCHAR(32) NOT NULL,
    penalty_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    arbitration_reason TEXT NOT NULL,
    audited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dispute_audits_fault_party ON sla_dispute_audits(final_fault_party, penalty_eligible);
