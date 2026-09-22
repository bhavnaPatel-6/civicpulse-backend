package com.civicPulse.civicPulse_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComplaintResolveRequest {

    @Size(max = 1000, message = "Note must be under 1000 characters")
    private String resolutionNote;

    @NotBlank(message = "Resolution proof photo is required")
    private String resolutionPhotoUrl;

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public String getResolutionPhotoUrl() { return resolutionPhotoUrl; }
    public void setResolutionPhotoUrl(String resolutionPhotoUrl) { this.resolutionPhotoUrl = resolutionPhotoUrl; }
}