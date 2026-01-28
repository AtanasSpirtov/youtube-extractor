package com.youtube.controller;

import com.youtube.external.rest.pythonclient.RagSystemRestApiClient;
import com.youtube.external.webflux.RagSystemWebFluxClient;
import com.youtube.external.rest.pythonclient.dto.EmbedTranscriptRequest;
import com.youtube.external.rest.pythonclient.dto.EmbedTranscriptResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class EmbeddingController {

    private final RagSystemRestApiClient client;

    public EmbeddingController(RagSystemRestApiClient client) {
        this.client = client;
    }

    //For debug
    @PostMapping("/embed")
    public EmbedTranscriptResponse embed(@RequestBody EmbedTranscriptRequest request) {
        EmbedTranscriptRequest withDefaults = new EmbedTranscriptRequest(
                request.text(),
                request.task() != null ? request.task() : "retrieval.passage",
                request.chunkTokens() != null ? request.chunkTokens() : 1024,
                request.chunkOverlap() != null ? request.chunkOverlap() : 128,
                request.normalize() != null ? request.normalize() : Boolean.TRUE
        );

        return client.embedTranscript(withDefaults);
    }
}