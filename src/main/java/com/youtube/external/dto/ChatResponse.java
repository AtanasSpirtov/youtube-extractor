package com.youtube.external.dto;

public record ChatResponse(
        String answer,
        String modelPath,
        int historySize
) {}
