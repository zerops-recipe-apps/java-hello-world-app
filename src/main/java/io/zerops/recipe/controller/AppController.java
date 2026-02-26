package io.zerops.recipe.controller;

import io.zerops.recipe.repository.GreetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AppController {

    @Autowired
    private GreetingRepository greetingRepository;

    /**
     * Health check and greeting endpoint.
     *
     * Returns HTTP 200 with database status and the greeting message
     * seeded by the migration. Returns HTTP 503 with an error detail
     * if the database is unreachable or the greetings table is missing.
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, String> status   = new LinkedHashMap<>();
        response.put("type", "java");

        try {
            String greeting = greetingRepository.findById(1)
                .map(g -> g.getMessage())
                .orElse("No greeting found — migration may not have run.");

            response.put("greeting", greeting);
            status.put("database", "OK");
            response.put("status", status);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("greeting", null);
            status.put("database", "ERROR: " + e.getMessage());
            response.put("status", status);
            return ResponseEntity.status(503).body(response);
        }
    }
}
