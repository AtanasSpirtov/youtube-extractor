package com.youtube.external;

import com.youtube.external.dto.ChatRequest;
import com.youtube.external.dto.ChatResponse;
import com.youtube.external.dto.TranscriptResponse;
import com.youtube.external.exception.TranscriptRateLimitedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RagSystemApiClient {

    private final RestClient restClient;

    public ChatResponse chat(ChatRequest request) {
        return restClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
    }

//    @RateLimiter(name = "transcript")
//    @Retry(name = "transcript")
//    @CircuitBreaker(name = "transcript429")
    public TranscriptResponse transcript(String videoId, List<String> languages) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        UriBuilder b = uriBuilder.path("/youtube/transcript")
                                .queryParam("video_id", videoId);
                        for (String lang : languages) b.queryParam("languages", lang);
                        return b.build();
                    })
                    .retrieve()
                    .body(TranscriptResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                throw new TranscriptRateLimitedException(e);
            }
            throw e;
        }
    }
}
