package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.AIValidationResult;
import com.civicPulse.civicPulse_backend.service.AIValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TestController {

    private final AIValidationService aiValidationService;

    public TestController(AIValidationService aiValidationService) {
        this.aiValidationService = aiValidationService;
    }

    @GetMapping("/api/test")
    public String test() {
        return "CivicPulse API is working!";
    }

    @GetMapping("/api/test/ai-validation")
    public ResponseEntity<AIValidationResult> testAiValidationGet(
            @RequestParam(defaultValue = "Huge pothole on MG Road") String title,
            @RequestParam(defaultValue = "A deep pothole near the bus stand causing accidents.") String description,
            @RequestParam(defaultValue = "Roads") String category,
            @RequestParam(required = false) String photoUrl) {

        return ResponseEntity.ok(aiValidationService.validate(title, description, category, photoUrl));
    }

    @PostMapping("/api/test/ai-validation")
    public ResponseEntity<AIValidationResult> testAiValidationPost(
            @RequestBody(required = false) Map<String, String> request) {

        String title = request != null ? request.getOrDefault("title", "Broken street light") : "Broken street light";
        String description = request != null ? request.getOrDefault("description", "Streetlight pole broken and dark at night") : "Streetlight pole broken and dark at night";
        String category = request != null ? request.getOrDefault("category", "Street Light") : "Street Light";
        String photoUrl = request != null ? request.get("photoUrl") : null;

        return ResponseEntity.ok(aiValidationService.validate(title, description, category, photoUrl));
    }
}