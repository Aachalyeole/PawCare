package com.pawcare.Pawcare_Backend.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String userEmail; // optional
}