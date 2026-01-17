package com.youtube.external;

import com.youtube.external.dto.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RagSystemApiClient {

    private final RestClient restClient;

    public RagSystemApiClient(RestClient restClient) {
        this.restClient = restClient;
    }


    // POST /generate
    public GenerateResponse generate(GenerateRequest request) {
        return restClient.post()
                .uri("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GenerateResponse.class);
    }

    // POST /chat
    public ChatResponse chat(ChatRequest request) {
        return restClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
    }

    // POST /chat/clear
    public void clearChat() {
        restClient.post()
                .uri("/chat/clear")
                .retrieve()
                .toBodilessEntity();
    }

    // GET /youtube/transcript?video_id=...&languages=...
    public TranscriptResponse transcript(String videoId, List<String> languages) {
        return restClient.get()
                .uri(uriBuilder -> {
                    var b = uriBuilder.path("/youtube/transcript")
                            .queryParam("video_id", videoId);
                    for (String lang : languages) {
                        b.queryParam("languages", lang);
                    }
                    return b.build();
                })
                .retrieve()
                .body(TranscriptResponse.class);
    }

    // Convenience overload with your default languages
    public TranscriptResponse transcript(String videoId) {
        return transcript(videoId, List.of("bgn", "bg", "en"));
    }
}
