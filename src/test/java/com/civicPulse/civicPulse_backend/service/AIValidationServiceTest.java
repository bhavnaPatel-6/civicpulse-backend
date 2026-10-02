package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.AIValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AIValidationServiceTest {

    private AIValidationService aiValidationService;

    @BeforeEach
    void setUp() {
        aiValidationService = new AIValidationService();
    }

    @Test
    void testValidCivicComplaint() {
        AIValidationResult result = aiValidationService.validate(
                "Huge pothole on MG Road",
                "There is a deep pothole near the bus stop causing severe traffic jams and risk of accidents.",
                "Roads & Infrastructure",
                null
        );

        assertNotNull(result);
        assertTrue(result.valid(), "Valid civic issue should be recognized as valid");
        assertFalse(result.spam(), "Should not be spam");
        assertTrue(result.isPublicIssue(), "Should be a public issue");
        assertEquals("HIGH", result.suggestedSeverity(), "Severe danger should trigger HIGH severity");
    }

    @Test
    void testDomesticMattersRejected() {
        AIValidationResult result = aiValidationService.validate(
                "Bhai ne room me kachra kiya",
                "My brother made a mess in our bedroom, please clean it.",
                "Sanitation",
                null
        );

        assertNotNull(result);
        assertFalse(result.valid(), "Domestic issue should not be valid");
        assertFalse(result.isPublicIssue(), "Domestic issue is not public");
        assertTrue(result.reason().toLowerCase().contains("domestic") || result.reason().toLowerCase().contains("private"));
    }

    @Test
    void testSpamRejected() {
        AIValidationResult result = aiValidationService.validate(
                "hi",
                "123",
                "General",
                null
        );

        assertNotNull(result);
        assertFalse(result.valid(), "Short/trivial text should not be valid");
        assertTrue(result.spam(), "Short text should be flagged as spam");
    }

    @Test
    void testBase64PhotoUrlDoesNotCrash() {
        // Base64 data URI should be handled smoothly without throwing IllegalArgumentException
        String base64Photo = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=";

        assertDoesNotThrow(() -> {
            AIValidationResult result = aiValidationService.validate(
                    "Garbage dump overflow",
                    "Municipal trash container overflowing on Main street with bad smell",
                    "Garbage Collection",
                    base64Photo
            );
            assertNotNull(result);
            assertTrue(result.valid());
            assertTrue(result.evidenceSufficient());
        });
    }
}
