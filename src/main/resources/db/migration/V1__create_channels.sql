create table if not exists channels (
    id bigserial primary key,
    youtube_channel_id varchar(64) not null unique,
    name varchar(255) not null);
