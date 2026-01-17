create table if not exists videos (
    id bigserial primary key,
    youtube_video_id varchar(64) not null unique,
    channel_id bigint not null,
    constraint fk_videos_channel
    foreign key (channel_id)
    references channels(id)
    );

create index if not exists ix_videos_channel_id on videos(channel_id);
