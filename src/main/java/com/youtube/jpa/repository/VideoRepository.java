package com.youtube.jpa.repository;

import com.youtube.jpa.dao.ChannelDao;
import com.youtube.jpa.dao.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

  boolean existsByYoutubeVideoId(String youtubeVideoId);

  List<Video> findAllByYoutubeVideoIdIn(Collection<String> youtubeVideoIds);

  Optional<Video> findByYoutubeVideoId(String youtubeVideoId);

  List<Video> findVideosByChannelDao(ChannelDao channel);
}
