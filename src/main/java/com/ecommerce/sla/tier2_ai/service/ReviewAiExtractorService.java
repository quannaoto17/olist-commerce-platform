package com.ecommerce.sla.tier2_ai.service;

import com.ecommerce.sla.tier2_ai.schema.AiReviewExtractionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Tier 2 Service: Calls external LLM REST API directly and parses JSON response into Java Record.
 * You have 100% control over the HTTP payload, System Prompt, and JSON parsing.
 */
@Service
public class ReviewAiExtractorService {

    private final RestClient llmRestClient;
    private final ObjectMapper objectMapper;

    @Value("${app.llm.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${app.llm.temperature:0.1}")
    private double temperature;

    public ReviewAiExtractorService(RestClient llmRestClient, ObjectMapper objectMapper) {
        this.llmRestClient = llmRestClient;
        this.objectMapper = objectMapper;
    }

    public AiReviewExtractionResult analyzeNegativeReview(String orderId, int reviewScore, String reviewComment) {
        String systemPrompt = """
                You are an objective E-Commerce Logistics Dispute Analyst.
                Analyze the customer review and respond ONLY with a valid JSON object matching this exact schema:
                {
                  "complaintCategory": "LATE_DELIVERY | DAMAGED_PACKAGE | LOST_ITEM | WRONG_OR_MISSING_PRODUCT | RUDE_COURIER | OTHER",
                  "blamedParty": "CARRIER_3PL | SELLER_MERCHANT | BOTH | UNCLEAR",
                  "evidenceSnippet": "Exact verbatim quote from the customer's review"
                }
                """;

        String userPrompt = String.format(
                "Order ID: %s | Review Score: %d/5 | Review Comment: \"%s\"",
                orderId, reviewScore, reviewComment
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", temperature,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        try {
            String rawResponse = llmRestClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(rawResponse);
            String contentJson = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readValue(contentJson, AiReviewExtractionResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract review via LLM API for order " + orderId, e);
        }
    }
}
