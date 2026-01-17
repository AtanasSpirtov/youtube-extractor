package com.youtube.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRequest(
        String message,
        @JsonProperty("max_tokens") int maxTokens,
        double temperature,
        @JsonProperty("top_p") double topP,
        @JsonProperty("repeat_penalty") double repeatPenalty,
        @JsonProperty("clear_history") boolean clearHistory
) {}
