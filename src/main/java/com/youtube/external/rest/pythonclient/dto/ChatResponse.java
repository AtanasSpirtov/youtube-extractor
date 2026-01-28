package com.youtube.external.rest.pythonclient.dto;

public record ChatResponse(
        String answer,
        String modelPath,
        int historySize
) {}
