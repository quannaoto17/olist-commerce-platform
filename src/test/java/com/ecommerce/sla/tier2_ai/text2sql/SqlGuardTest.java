package com.ecommerce.sla.tier2_ai.text2sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlGuardTest {

    @Test
    void acceptsSelectOnAllowedView() {
        assertEquals("SELECT seller_id, COUNT(*) FROM vw_tier1_order_sla_metrics WHERE is_sla_breached GROUP BY seller_id LIMIT 10",
                SqlGuard.validate("SELECT seller_id, COUNT(*) FROM vw_tier1_order_sla_metrics WHERE is_sla_breached GROUP BY seller_id LIMIT 10;"));
    }

    @Test
    void acceptsCteAndJoin() {
        SqlGuard.validate("WITH x AS (SELECT seller_id FROM olist_order_items) SELECT s.seller_city FROM olist_sellers s JOIN x ON x.seller_id = s.seller_id LIMIT 5");
    }

    @Test
    void rejectsWritesAndMultiStatements() {
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("DELETE FROM olist_orders"));
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("SELECT 1; DROP TABLE olist_orders"));
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("SELECT * INTO t FROM olist_orders"));
    }

    @Test
    void rejectsForeignRelationsAndSystemCatalogs() {
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("SELECT * FROM sla_dispute_audits"));
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("SELECT * FROM pg_user"));
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("SELECT * FROM information_schema.tables"));
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("SELECT 1 -- hi"));
    }

    @Test
    void keywordsInsideStringLiteralsAreIgnored() {
        SqlGuard.validate("SELECT review_id FROM olist_order_reviews WHERE review_comment_message LIKE '%delete%' LIMIT 5");
    }
}
