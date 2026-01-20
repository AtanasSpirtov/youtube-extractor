ALTER TABLE videos
    ADD COLUMN transcript_passed boolean NOT NULL DEFAULT false;

CREATE TABLE video_transcripts
(
    id              bigserial PRIMARY KEY,
    video_id        bigserial      NOT NULL UNIQUE,
    transcript_text text        NOT NULL,
    created_at      timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_video_transcripts_video
        FOREIGN KEY (video_id) REFERENCES videos (id)
)