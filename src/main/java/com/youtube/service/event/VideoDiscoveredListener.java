package com.youtube.service.event;

import com.youtube.external.RagSystemApiClient;
import com.youtube.external.dto.TranscriptResponse;
import com.youtube.jpa.dao.Video;
import com.youtube.jpa.dao.VideoTranscript;
import com.youtube.jpa.repository.VideoRepository;
import com.youtube.jpa.repository.VideoTranscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDiscoveredListener {

    private final RagSystemApiClient transcriptClient;
    private final VideoRepository videoRepository;
    private final VideoTranscriptRepository transcriptRepository;

    @Async("transcriptExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVideoDiscovered(VideoDiscoveredEvent event) {
        String youtubeVideoId = event.youtubeVideoId();

        log.info(
                "[TRANSCRIPT][START] videoId={} thread={}",
                youtubeVideoId,
                Thread.currentThread().getName()
        );

        Video video = videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .orElseThrow(() -> new IllegalStateException("Video not found in DB for youtubeVideoId=" + youtubeVideoId));

        if (video.isTranscriptPassed() && transcriptRepository.existsByVideoId(video.getId())) {
            log.error(
                    "[TRANSCRIPT][SKIP] already processed videoId={} videoDbId={}",
                    youtubeVideoId,
                    video.getId()
            );
            return;
        }

        try {
            log.info("[TRANSCRIPT][CALL] calling transcript API videoId={}", youtubeVideoId);
            TranscriptResponse resp = transcriptClient.transcript(youtubeVideoId, List.of("bgn", "bg"));

            String text = resp.text();
            if (text == null || text.isBlank()) {
                video.setTranscriptPassed(false);
                log.warn("Empty transcript for youtubeVideoId={}", youtubeVideoId);
                return;
            }

            transcriptRepository.findByVideoId(video.getId())
                    .ifPresentOrElse(existing -> {
                        existing.setTranscriptText(text);
                        transcriptRepository.save(existing);
                        log.info(
                                "[TRANSCRIPT][UPDATE] videoId={} chars={}",
                                youtubeVideoId,
                                text.length()
                        );
                    }, () -> {
                        transcriptRepository.save(VideoTranscript.builder()
                                .video(video)
                                .transcriptText(text)
                                .build());
                        log.info(
                                "[TRANSCRIPT][INSERT] videoId={} chars={}",
                                youtubeVideoId,
                                text.length()
                        );
                    });

            video.setTranscriptPassed(true);
        } catch (HttpClientErrorException e) {
            video.setTranscriptPassed(false);

            int status = e.getStatusCode().value();

            if (status == 429) {
                log.warn(
                        "[TRANSCRIPT][RATE_LIMIT] videoId={} status={} msg={}",
                        youtubeVideoId,
                        status,
                        e.getMessage()
                );
            } else {
                log.warn(
                        "[TRANSCRIPT][HTTP_ERROR] videoId={} status={} msg={}",
                        youtubeVideoId,
                        status,
                        e.getMessage()
                );
            }

            throw e;

        } catch (Exception e) {
            video.setTranscriptPassed(false);

            log.error(
                    "[TRANSCRIPT][FAIL] videoId={} msg={}",
                    youtubeVideoId,
                    e.getMessage(),
                    e
            );
        }
    }
}
