-- ============================================================================
-- V2: Tier 1 Quantitative SLA Calculation View
-- Computes Seller Handover Delay vs 3PL Carrier Transit Delay
-- ============================================================================

CREATE OR REPLACE VIEW vw_tier1_order_sla_metrics AS
SELECT
    o.order_id,
    o.customer_id,
    oi.seller_id,
    o.order_status,
    o.order_purchase_timestamp,
    o.order_approved_at,
    oi.shipping_limit_date,
    o.order_delivered_carrier_date,
    o.order_delivered_customer_date,
    o.order_estimated_delivery_date,
    r.review_id,
    r.review_score,
    r.review_comment_title,
    r.review_comment_message,

    -- Total delivery delay in days vs estimated SLA (> 0 means late delivery to buyer)
    ROUND(EXTRACT(EPOCH FROM (o.order_delivered_customer_date - o.order_estimated_delivery_date)) / 86400.0, 2)
        AS total_delivery_delay_days,

    -- Seller handover delay in days vs shipping_limit_date (> 0 means Seller handed over late to 3PL)
    ROUND(EXTRACT(EPOCH FROM (o.order_delivered_carrier_date - oi.shipping_limit_date)) / 86400.0, 2)
        AS seller_handover_delay_days,

    -- Boolean flags for Tier 1 quantitative breach verification
    (o.order_delivered_customer_date > o.order_estimated_delivery_date) AS is_sla_breached,
    (o.order_delivered_carrier_date > oi.shipping_limit_date) AS is_seller_handover_late
FROM olist_orders o
JOIN olist_order_items oi ON o.order_id = oi.order_id AND oi.order_item_id = 1
LEFT JOIN olist_order_reviews r ON o.order_id = r.order_id;
