package com.youtube.jpa.repository;

import com.youtube.jpa.dao.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

  boolean existsByYoutubeVideoId(String youtubeVideoId);

  List<Video> findAllByYoutubeVideoIdIn(Collection<String> youtubeVideoIds);
}
