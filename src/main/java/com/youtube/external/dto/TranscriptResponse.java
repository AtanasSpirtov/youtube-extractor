package com.youtube.external.dto;

public record TranscriptResponse(
        String videoId,
        String language,
        String text
) {}
