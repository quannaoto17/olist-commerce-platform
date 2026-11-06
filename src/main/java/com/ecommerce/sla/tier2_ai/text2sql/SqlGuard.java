package com.ecommerce.sla.tier2_ai.text2sql;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates LLM-generated SQL before execution: a single read-only SELECT over whitelisted relations only.
 * This is defence in depth; the query is also executed in a read-only transaction with a timeout.
 */
public final class SqlGuard {

    public static final Set<String> ALLOWED_RELATIONS = Set.of(
            "olist_customers", "olist_sellers", "olist_orders", "olist_order_items",
            "olist_order_reviews", "vw_tier1_order_sla_metrics");

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|grant|revoke|truncate|copy|call|do|execute|merge|vacuum|"
                    + "set|reset|listen|notify|lock|into|pg_\\w*|lo_\\w*|dblink\\w*|information_schema|current_setting|"
                    + "set_config)\\b");
    private static final Pattern RELATION = Pattern.compile("\\b(?:from|join)\\s+([a-z_][a-z0-9_\\.\"]*)");
    private static final Pattern CTE_NAME = Pattern.compile("\\b([a-z_][a-z0-9_]*)\\s+as\\s*\\(");

    private SqlGuard() {
    }

    /** Returns the cleaned single statement (no trailing semicolon) or throws IllegalArgumentException. */
    public static String validate(String rawSql) {
        if (rawSql == null || rawSql.isBlank()) {
            throw new IllegalArgumentException("Empty SQL");
        }
        String sql = rawSql.trim();
        if (sql.startsWith("```")) {
            sql = sql.replaceAll("^```(?:sql)?", "").replaceAll("```$", "").trim();
        }
        if (sql.contains("--") || sql.contains("/*")) {
            throw new IllegalArgumentException("SQL comments are not allowed");
        }
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        if (sql.contains(";")) {
            throw new IllegalArgumentException("Only a single statement is allowed");
        }

        String lower = sql.toLowerCase(Locale.ROOT);
        // Ignore string literals when scanning for keywords/relations.
        String scan = lower.replaceAll("'(?:[^']|'')*'", "''");

        if (!(scan.startsWith("select") || scan.startsWith("with"))) {
            throw new IllegalArgumentException("Only SELECT queries are allowed");
        }
        Matcher bad = FORBIDDEN.matcher(scan);
        if (bad.find()) {
            throw new IllegalArgumentException("Forbidden keyword: " + bad.group(1));
        }

        Set<String> ctes = new HashSet<>();
        Matcher c = CTE_NAME.matcher(scan);
        while (c.find()) {
            ctes.add(c.group(1));
        }
        Matcher r = RELATION.matcher(scan);
        while (r.find()) {
            String rel = r.group(1).replace("\"", "");
            if (rel.startsWith("public.")) {
                rel = rel.substring("public.".length());
            }
            if (!ALLOWED_RELATIONS.contains(rel) && !ctes.contains(rel)) {
                throw new IllegalArgumentException("Relation not allowed: " + rel);
            }
        }
        return sql;
    }
}
