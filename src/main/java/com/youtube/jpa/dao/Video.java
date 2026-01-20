package com.youtube.jpa.dao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "videos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Video {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "youtube_video_id", nullable = false, unique = true, length = 64)
  private String youtubeVideoId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private ChannelDao channelDao;

  @Column(name = "transcript_passed", nullable = false)
  private boolean transcriptPassed;

}
