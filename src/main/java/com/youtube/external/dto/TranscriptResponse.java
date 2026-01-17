package com.youtube.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TranscriptResponse(
        @JsonProperty("video_id") String videoId,
        String language,
        List<TranscriptSnippet> snippets,
        String text
) {}
