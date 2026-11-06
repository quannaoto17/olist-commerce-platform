package com.ecommerce.sla.tier2_ai.text2sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Natural language question -> guarded read-only SQL -> rows. */
@Service
public class TextToSqlService {

    static final int MAX_ROWS = 200;
    static final int TIMEOUT_MS = 5000;

    private static final String SCHEMA = """
            PostgreSQL schema (Olist e-commerce, Brazil). Use ONLY these relations:
            olist_customers(customer_id, customer_unique_id, customer_zip_code_prefix, customer_city, customer_state)
            olist_sellers(seller_id, seller_zip_code_prefix, seller_city, seller_state)
            olist_orders(order_id, customer_id, order_status, order_purchase_timestamp, order_approved_at,
                         order_delivered_carrier_date, order_delivered_customer_date, order_estimated_delivery_date)
            olist_order_items(order_id, order_item_id, product_id, seller_id, shipping_limit_date, price, freight_value)
            olist_order_reviews(review_id, order_id, review_score 1-5, review_comment_title, review_comment_message,
                                review_creation_date, review_answer_timestamp)
            vw_tier1_order_sla_metrics(order_id, customer_id, seller_id, order_status, order_purchase_timestamp,
                order_approved_at, shipping_limit_date, order_delivered_carrier_date, order_delivered_customer_date,
                order_estimated_delivery_date, review_id, review_score, review_comment_title, review_comment_message,
                total_delivery_delay_days (>0 = late to buyer), seller_handover_delay_days (>0 = seller late to carrier),
                is_sla_breached boolean, is_seller_handover_late boolean)
            Prefer vw_tier1_order_sla_metrics for SLA / delay / review questions.
            """;

    private final RestClient llmRestClient;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.llm.model:llama-3.3-70b-versatile}")
    private String model;

    public TextToSqlService(RestClient llmRestClient, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.llmRestClient = llmRestClient;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public record Result(String question, String sql, List<String> columns, List<Map<String, Object>> rows,
                         boolean truncated) {
    }

    public Result ask(String question) {
        String sql = SqlGuard.validate(generateSql(question));
        return execute(question, sql);
    }

    String generateSql(String question) {
        String system = """
                You translate questions (Vietnamese or English) into ONE PostgreSQL SELECT statement.
                Respond ONLY with JSON: {"sql": "<query>"}. No comments, no semicolons, read-only.
                Always add a LIMIT (max %d) unless the query is a single aggregate row.
                """.formatted(MAX_ROWS) + SCHEMA;

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", question)));
        try {
            String raw = llmRestClient.post().body(body).retrieve().body(String.class);
            String content = objectMapper.readTree(raw).path("choices").get(0).path("message").path("content").asText();
            JsonNode node = objectMapper.readTree(content);
            return node.path("sql").asText();
        } catch (Exception e) {
            throw new IllegalStateException("LLM failed to generate SQL", e);
        }
    }

    private Result execute(String question, String sql) {
        return jdbcTemplate.execute((Connection con) -> {
            boolean oldAuto = con.getAutoCommit();
            boolean oldRo = con.isReadOnly();
            try {
                con.setAutoCommit(false);
                con.setReadOnly(true);
                try (Statement st = con.createStatement()) {
                    st.execute("SET LOCAL statement_timeout = " + TIMEOUT_MS);
                    st.setMaxRows(MAX_ROWS + 1);
                    try (ResultSet rs = st.executeQuery(sql)) {
                        ResultSetMetaData md = rs.getMetaData();
                        List<String> cols = new ArrayList<>();
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            cols.add(md.getColumnLabel(i));
                        }
                        List<Map<String, Object>> rows = new ArrayList<>();
                        boolean truncated = false;
                        while (rs.next()) {
                            if (rows.size() == MAX_ROWS) {
                                truncated = true;
                                break;
                            }
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= cols.size(); i++) {
                                Object v = rs.getObject(i);
                                row.put(cols.get(i - 1), v == null || v instanceof Number || v instanceof Boolean
                                        ? v : v.toString());
                            }
                            rows.add(row);
                        }
                        return new Result(question, sql, cols, rows, truncated);
                    }
                }
            } finally {
                con.rollback();
                con.setReadOnly(oldRo);
                con.setAutoCommit(oldAuto);
            }
        });
    }
}
