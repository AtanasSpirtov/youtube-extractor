package com.youtube.external.exception;

public class TranscriptRateLimitedException extends RuntimeException {
    public TranscriptRateLimitedException(Throwable cause) { super(cause); }
}
