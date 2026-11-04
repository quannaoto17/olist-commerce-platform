-- ============================================================================
-- V1: Olist E-Commerce Core Relational Schema (Tier 1 Raw Tables)
-- ============================================================================

CREATE TABLE IF NOT EXISTS olist_customers (
    customer_id VARCHAR(64) PRIMARY KEY,
    customer_unique_id VARCHAR(64) NOT NULL,
    customer_zip_code_prefix VARCHAR(16),
    customer_city VARCHAR(128),
    customer_state VARCHAR(16)
);

CREATE TABLE IF NOT EXISTS olist_sellers (
    seller_id VARCHAR(64) PRIMARY KEY,
    seller_zip_code_prefix VARCHAR(16),
    seller_city VARCHAR(128),
    seller_state VARCHAR(16)
);

CREATE TABLE IF NOT EXISTS olist_orders (
    order_id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL REFERENCES olist_customers(customer_id),
    order_status VARCHAR(32) NOT NULL,
    order_purchase_timestamp TIMESTAMP NOT NULL,
    order_approved_at TIMESTAMP,
    order_delivered_carrier_date TIMESTAMP,
    order_delivered_customer_date TIMESTAMP,
    order_estimated_delivery_date TIMESTAMP NOT NULL
);

CREATE INDEX idx_orders_status ON olist_orders(order_status);
CREATE INDEX idx_orders_dates ON olist_orders(order_delivered_customer_date, order_estimated_delivery_date);

CREATE TABLE IF NOT EXISTS olist_order_items (
    order_id VARCHAR(64) NOT NULL REFERENCES olist_orders(order_id),
    order_item_id INT NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    seller_id VARCHAR(64) NOT NULL REFERENCES olist_sellers(seller_id),
    shipping_limit_date TIMESTAMP NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    freight_value NUMERIC(12, 2) NOT NULL,
    PRIMARY KEY (order_id, order_item_id)
);

CREATE INDEX idx_order_items_seller ON olist_order_items(seller_id);

CREATE TABLE IF NOT EXISTS olist_order_reviews (
    review_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL REFERENCES olist_orders(order_id),
    review_score INT NOT NULL CHECK (review_score BETWEEN 1 AND 5),
    review_comment_title TEXT,
    review_comment_message TEXT,
    review_creation_date TIMESTAMP,
    review_answer_timestamp TIMESTAMP,
    PRIMARY KEY (review_id, order_id)
);

-- Partial index optimizing Risk Matrix #3: Only feed low-score reviews (<= 2 stars) with text to AI
CREATE INDEX idx_reviews_low_score_with_comment
    ON olist_order_reviews(order_id, review_score)
    WHERE review_score <= 2 AND review_comment_message IS NOT NULL;
