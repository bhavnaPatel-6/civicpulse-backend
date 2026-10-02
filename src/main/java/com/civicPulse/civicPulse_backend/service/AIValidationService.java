package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.AIValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIValidationService {

    private static final Logger log = LoggerFactory.getLogger(AIValidationService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openaiModel;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    // Active vision-capable models on Groq
    private static final List<String> GROQ_VISION_MODELS = List.of(
            "llama-3.2-11b-vision-preview",
            "llama-3.2-90b-vision-preview"
    );

    // Active text fallback models on Groq
    private static final List<String> GROQ_TEXT_MODELS = List.of(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant"
    );

    public AIValidationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    public AIValidationResult validate(String title, String description, String categoryName, String photoUrl) {
        boolean hasGroqKey = groqApiKey != null && !groqApiKey.isBlank();
        boolean hasOpenAiKey = openaiApiKey != null && !openaiApiKey.isBlank();
        boolean hasPhoto = photoUrl != null && !photoUrl.isBlank();

        String promptText = buildPrompt(title, description, categoryName, hasPhoto);

        // 1. Try Groq if key is provided
        if (hasGroqKey) {
            AIValidationResult result = callGroq(promptText, photoUrl);
            if (result != null) {
                return result;
            }
        }

        // 2. Try OpenAI if key is provided
        if (hasOpenAiKey) {
            AIValidationResult result = callOpenAI(promptText, photoUrl);
            if (result != null) {
                return result;
            }
        }

        // 3. Fallback to smart heuristic validator if API keys are missing or calls failed
        String note = (!hasGroqKey && !hasOpenAiKey)
                ? " (Civic heuristic: AI API key not configured)"
                : " (Civic heuristic fallback: AI API temporary error)";

        log.info("Using heuristic civic validation for '{}'{}", title, note);
        return heuristicValidate(title, description, categoryName, photoUrl, note);
    }

    private String buildPrompt(String title, String description, String categoryName, boolean hasPhoto) {
        return String.format("""
            You are a strict Municipal Civic Issue Validator and Fraud Detection Agent.
            Analyze the provided complaint details and photograph. Citizens write in English, Hindi, or Hinglish.

            STRICT VALIDATION RULES:
            1. CIVIC RELEVANCE:
               - Valid complaints concern public street, municipal infrastructure, sanitation, or community issues:
                 potholes, broken roads, open manholes, garbage accumulation, water pipeline leaks, streetlights, drainage, sewage overflow, fallen electric poles/wires, traffic hazards.
               - DOMESTIC MATTERS MUST BE REJECTED (valid: false, isPublicIssue: false):
                 Reject room cleaning, kitchen chores, private house/apartment issues, sibling/family pranks ('bhai ne room me kachra kiya', 'ghar').
               - SPAM OR GIBBERISH MUST BE REJECTED (valid: false, spam: true):
                 Random text, test messages, memes, abuse.
            2. VISUAL VERIFICATION:
               - If an image is provided, inspect its content.
               - REJECT (valid: false, evidenceSufficient: false, isPublicIssue: false) if:
                 * Image shows private indoor space (bedroom, kitchen, bathroom, household furniture).
                 * Image does NOT match the civic issue (e.g. text says pothole/garbage dump, but photo shows pets/cat, people, clean road, vehicles, or food).
                 * Image is blurry, meme, illustration, or irrelevant.
               - If photo is not provided or cannot be fetched, evaluate based on description and categorize accordingly.
            3. ACCEPT AS VALID (valid: true, isPublicIssue: true):
               - Genuine public municipal street issues where description/photo confirms civic damage.

            Complaint:
            - Category: %s
            - Title: %s
            - Description: %s
            - Photo Attached: %s

            Respond with ONLY a raw JSON object (strictly no markdown formatting, no backticks, no ```json):
            {
              "valid": boolean,
              "spam": boolean,
              "isPublicIssue": boolean,
              "evidenceSufficient": boolean,
              "suggestedSeverity": "LOW" | "MEDIUM" | "HIGH",
              "reason": "Clear explanation under 15 words"
            }
            """,
            categoryName != null ? categoryName : "General",
            title != null ? title : "",
            description != null ? description : "",
            hasPhoto ? "Yes" : "No"
        );
    }

    private AIValidationResult callGroq(String promptText, String photoUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey.trim());

        boolean hasPhoto = photoUrl != null && !photoUrl.isBlank();
        List<String> modelsToTry = new ArrayList<>();

        if (hasPhoto) {
            modelsToTry.addAll(GROQ_VISION_MODELS);
        }
        modelsToTry.addAll(GROQ_TEXT_MODELS);

        Exception lastException = null;

        for (String model : modelsToTry) {
            boolean isVisionModel = GROQ_VISION_MODELS.contains(model);
            boolean sendImage = hasPhoto && isVisionModel;

            try {
                List<Map<String, Object>> content = buildUserContent(promptText, photoUrl, sendImage);
                Map<String, Object> body = Map.of(
                        "model", model,
                        "messages", List.of(Map.of("role", "user", "content", content)),
                        "temperature", 0.1,
                        "response_format", Map.of("type", "json_object")
                );

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(GROQ_URL, entity, String.class);

                AIValidationResult result = parseAiResponse(response.getBody());
                if (result != null) {
                    return result;
                }
            } catch (Exception ex) {
                log.warn("Groq model {} failed: {}", model, ex.getMessage());
                lastException = ex;
            }
        }

        log.error("All Groq candidate models failed. Last error: {}",
                lastException != null ? lastException.getMessage() : "unknown");
        return null;
    }

    private AIValidationResult callOpenAI(String promptText, String photoUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey.trim());

        boolean hasPhoto = photoUrl != null && !photoUrl.isBlank();

        try {
            List<Map<String, Object>> content = buildUserContent(promptText, photoUrl, hasPhoto);
            Map<String, Object> body = Map.of(
                    "model", (openaiModel != null && !openaiModel.isBlank()) ? openaiModel : "gpt-4o-mini",
                    "messages", List.of(Map.of("role", "user", "content", content)),
                    "temperature", 0.1,
                    "response_format", Map.of("type", "json_object")
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, entity, String.class);

            return parseAiResponse(response.getBody());
        } catch (Exception ex) {
            log.warn("OpenAI API call failed: {}", ex.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> buildUserContent(String promptText, String photoUrl, boolean includeImage) {
        List<Map<String, Object>> contentList = new ArrayList<>();
        contentList.add(Map.of("type", "text", "text", promptText));

        if (includeImage && photoUrl != null && !photoUrl.isBlank()) {
            String trimmed = photoUrl.trim();
            if (trimmed.startsWith("data:image/")) {
                // Directly pass base64 data URI
                contentList.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of("url", trimmed)
                ));
            } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                try {
                    byte[] imageBytes = restTemplate.getForObject(trimmed, byte[].class);
                    if (imageBytes != null && imageBytes.length > 0) {
                        String mimeType = trimmed.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                        String b64 = Base64.getEncoder().encodeToString(imageBytes);
                        contentList.add(Map.of(
                                "type", "image_url",
                                "image_url", Map.of("url", "data:" + mimeType + ";base64," + b64)
                        ));
                    } else {
                        contentList.add(Map.of(
                                "type", "image_url",
                                "image_url", Map.of("url", trimmed)
                        ));
                    }
                } catch (Exception e) {
                    log.warn("Image fetch from URL failed, passing URL directly: {}", e.getMessage());
                    contentList.add(Map.of(
                            "type", "image_url",
                            "image_url", Map.of("url", trimmed)
                    ));
                }
            }
        }

        return contentList;
    }

    private AIValidationResult parseAiResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String rawJson = root.path("choices").get(0)
                    .path("message").path("content").asText().trim();

            int firstBrace = rawJson.indexOf('{');
            int lastBrace = rawJson.lastIndexOf('}');
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                rawJson = rawJson.substring(firstBrace, lastBrace + 1);
            }

            JsonNode res = objectMapper.readTree(rawJson);
            boolean valid = res.path("valid").asBoolean(false);
            boolean spam = res.path("spam").asBoolean(false);
            boolean isPublic = res.path("isPublicIssue").asBoolean(false);
            boolean evidence = res.path("evidenceSufficient").asBoolean(false);
            String severity = res.path("suggestedSeverity").asText("MEDIUM").toUpperCase();
            String reason = res.path("reason").asText("");

            if (reason.length() > 450) {
                reason = reason.substring(0, 450);
            }

            return new AIValidationResult(valid, spam, isPublic, evidence, severity, reason);
        } catch (Exception ex) {
            log.error("Failed to parse AI response JSON: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Fallback heuristic validator when AI API key is missing or external API is unavailable.
     * Evaluates civic issue authenticity, filters domestic matters, and detects spam/gibberish.
     */
    public AIValidationResult heuristicValidate(String title, String description, String categoryName, String photoUrl, String note) {
        String combined = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        // 1. Spam or trivial text
        if (combined.trim().length() < 12) {
            return new AIValidationResult(false, true, false, false, "LOW", "Text too short or meaningless" + note);
        }

        List<String> spamKeywords = List.of("test", "testing", "asdf", "qwerty", "fake", "spam", "haha", "sample", "check check");
        for (String sp : spamKeywords) {
            if (combined.contains(sp)) {
                return new AIValidationResult(false, true, false, false, "LOW", "Flagged as test or spam text" + note);
            }
        }

        // 2. Domestic or private household matters
        List<String> domesticKeywords = List.of(
                "bedroom", "bed room", "kitchen", "bathroom", "sofa", "pillow",
                "bhai ne", "sister ne", "mummy", "room clean", "ghar ka", "ghar me",
                "prank", "chores", "personal room"
        );
        for (String kw : domesticKeywords) {
            if (combined.contains(kw)) {
                return new AIValidationResult(false, false, false, false, "LOW",
                        "Domestic or private issue not under municipal purview" + note);
            }
        }

        // 3. Known civic keywords (English + Hindi/Hinglish)
        List<String> civicKeywords = List.of(
                "pothole", "gaddha", "road", "sadak", "street", "garbage", "kachra",
                "trash", "dump", "waste", "light", "streetlight", "bijli", "water",
                "paani", "leak", "pipeline", "pipe", "drain", "drainage", "sewer",
                "naali", "manhole", "traffic", "signal", "wire", "pole", "encroach",
                "footpath", "sidewalk", "park", "tree", "fallen", "broken"
        );

        boolean matchesCivic = false;
        for (String kw : civicKeywords) {
            if (combined.contains(kw)) {
                matchesCivic = true;
                break;
            }
        }

        if (categoryName != null && !categoryName.isBlank()) {
            String catLower = categoryName.toLowerCase();
            if (catLower.contains("road") || catLower.contains("garbage") || catLower.contains("water")
                    || catLower.contains("drain") || catLower.contains("light") || catLower.contains("sanitation")
                    || catLower.contains("traffic") || catLower.contains("clean")) {
                matchesCivic = true;
            }
        }

        boolean hasPhoto = photoUrl != null && !photoUrl.isBlank();

        // Estimate severity
        String severity = "MEDIUM";
        if (combined.contains("accident") || combined.contains("danger") || combined.contains("flood")
                || combined.contains("fire") || combined.contains("shock") || combined.contains("severe")
                || combined.contains("huge") || combined.contains("overflow") || combined.contains("emergency")) {
            severity = "HIGH";
        } else if (combined.contains("minor") || combined.contains("small")) {
            severity = "LOW";
        }

        if (matchesCivic) {
            return new AIValidationResult(
                    true,
                    false,
                    true,
                    hasPhoto,
                    severity,
                    (hasPhoto ? "Civic issue verified with evidence" : "Civic issue verified from details") + note
            );
        } else {
            return new AIValidationResult(
                    false,
                    false,
                    false,
                    hasPhoto,
                    "LOW",
                    "Needs clarification or insufficient civic details" + note
            );
        }
    }
}