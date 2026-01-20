package com.youtube.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.youtube.jpa.dao.ChannelDao;
import com.youtube.jpa.dao.Video;
import com.youtube.jpa.repository.ChannelRepository;
import com.youtube.jpa.repository.VideoRepository;
import com.youtube.service.event.VideoDiscoveredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class YouTubeChannelVideosService {

    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final String apiKey;
    private final YouTube youtube;

    public YouTubeChannelVideosService(
            ChannelRepository channelRepository,
            VideoRepository videoRepository,
            @Value("${youtube.api-key}") String apiKey,
            ApplicationEventPublisher applicationEventPublisher
    ) throws Exception {
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.apiKey = apiKey;
        this.applicationEventPublisher = applicationEventPublisher;

        HttpRequestInitializer noAuth = request -> {
            request.setConnectTimeout(30_000);
            request.setReadTimeout(30_000);
        };

        this.youtube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                noAuth
        ).setApplicationName("youtube-extractor").build();
    }

    @Transactional
    public List<String> fetchAndSaveAllVideoIdsByHandle(String handle) throws Exception {
        String normalizedHandle = extractFromLink(handle);

        Channel ytChannel = fetchChannelByHandle(normalizedHandle);

        String youtubeChannelId = ytChannel.getId();
        String channelName = Optional.ofNullable(ytChannel.getSnippet())
                .map(ChannelSnippet::getTitle)
                .filter(t -> !t.isBlank())
                .orElse(normalizedHandle);

        String uploadsPlaylistId = ytChannel.getContentDetails()
                .getRelatedPlaylists()
                .getUploads();

        ChannelDao persistedChannelDao = upsertChannel(youtubeChannelId, channelName);

        List<String> videoIds = fetchAllVideoIdsFromUploads(uploadsPlaylistId);

        List<String> uniqueVideoIds = videoIds.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        Set<String> existingIds = videoRepository.findAllByYoutubeVideoIdIn(uniqueVideoIds).stream()
                .map(Video::getYoutubeVideoId)
                .collect(Collectors.toUnmodifiableSet());

        List<Video> videosForInsert = uniqueVideoIds.stream()
                .filter(id -> !existingIds.contains(id))
                .map(id -> Video.builder()
                        .youtubeVideoId(id)
                        .channelDao(persistedChannelDao)
                        .build())
                .toList();

        if (!videosForInsert.isEmpty()) {
            videoRepository.saveAll(videosForInsert);

            Long channelDbId = persistedChannelDao.getId();
            for (Video v : videosForInsert) {
                applicationEventPublisher.publishEvent(new VideoDiscoveredEvent(v.getYoutubeVideoId(), channelDbId));
            }
        }

        return videoIds;
    }

    private com.google.api.services.youtube.model.Channel fetchChannelByHandle(String normalizedHandle) throws Exception {
        ChannelListResponse channelResp = youtube.channels()
                .list(List.of("id", "contentDetails", "snippet"))
                .setForHandle(normalizedHandle)
                .setKey(apiKey)
                .execute();

        return Optional.ofNullable(channelResp.getItems())
                .filter(items -> !items.isEmpty())
                .map(List::getFirst)
                .orElseThrow(() -> new IllegalArgumentException("No channel found for handle: " + normalizedHandle));
    }

    private ChannelDao upsertChannel(String youtubeChannelId, String channelName) {
        Function<ChannelDao, ChannelDao> applyUpdates = existing -> {
            existing.setName(channelName);
            return existing;
        };

        ChannelDao channelDao = channelRepository.findByYoutubeChannelId(youtubeChannelId)
                .map(applyUpdates)
                .orElseGet(() -> ChannelDao.builder()
                        .youtubeChannelId(youtubeChannelId)
                        .name(channelName)
                        .build());

        return channelRepository.save(channelDao);
    }

    /**
     * Functional-ish paging: iterative, but all mutations are contained.
     * If you want *pure* recursion, Java doesn't optimize tail calls, so this is the safe approach.
     */
    private List<String> fetchAllVideoIdsFromUploads(String uploadsPlaylistId) throws Exception {
        List<String> result = new ArrayList<>();
        String pageToken = null;

        do {
            PlaylistItemListResponse plResp = youtube.playlistItems()
                    .list(List.of("contentDetails"))
                    .setPlaylistId(uploadsPlaylistId)
                    .setMaxResults(50L)
                    .setPageToken(pageToken)
                    .setKey(apiKey)
                    .execute();

            Stream<String> pageVideoIds = Optional.ofNullable(plResp.getItems())
                    .orElseGet(List::of)
                    .stream()
                    .map(PlaylistItem::getContentDetails)
                    .filter(Objects::nonNull)
                    .map(cd -> cd.getVideoId())
                    .filter(Objects::nonNull)
                    .filter(id -> !id.isBlank());

            pageVideoIds.forEach(result::add);
            pageToken = plResp.getNextPageToken();

        } while (pageToken != null && !pageToken.isBlank());

        return List.copyOf(result);
    }

    private String extractFromLink(String handle) {
        if (handle == null || handle.isBlank()) {
            throw new IllegalArgumentException("Handle is required");
        }
        String h = handle.trim();
        int at = h.indexOf("@");
        if (at >= 0) h = h.substring(at);
        if (!h.startsWith("@")) h = "@" + h;
        return h;
    }
}
