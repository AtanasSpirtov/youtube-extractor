package com.youtube.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerateRequest(
        String prompt,
        @JsonProperty("max_tokens") int maxTokens,
        double temperature,
        @JsonProperty("top_p") double topP,
        @JsonProperty("repeat_penalty") double repeatPenalty,
        List<String> stop
) {}
