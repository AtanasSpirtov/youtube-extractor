package com.youtube.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatResponse(
        String answer,
        @JsonProperty("model_path") String modelPath,
        @JsonProperty("history_size") int historySize
) {}
