package com.youtube.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record GenerateResponse(
        String text,
        Map<String, Object> usage,
        @JsonProperty("model_path") String modelPath
) {}
