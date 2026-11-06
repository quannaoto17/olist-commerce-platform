package com.ecommerce.sla.tier2_ai.text2sql;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ask")
public class TextToSqlController {

    public record AskRequest(@NotBlank @Size(max = 500) String question) {
    }

    private final TextToSqlService service;

    public TextToSqlController(TextToSqlService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> ask(@Valid @RequestBody AskRequest req) {
        try {
            return ResponseEntity.ok(service.ask(req.question()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "Generated SQL was rejected: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Could not answer the question: " + e.getMessage()));
        }
    }
}
