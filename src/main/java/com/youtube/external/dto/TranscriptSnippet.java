package com.youtube.external.dto;

public record TranscriptSnippet(
        String text,
        double start,
        double duration
) {}
