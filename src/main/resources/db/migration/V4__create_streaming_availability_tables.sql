CREATE TABLE streaming_platforms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    website_url VARCHAR(500),
    CONSTRAINT pk_streaming_platforms PRIMARY KEY (id),
    CONSTRAINT uk_streaming_platforms_name UNIQUE (name)
);

CREATE TABLE anime_streaming_availabilities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    anime_id BIGINT NOT NULL,
    streaming_platform_id BIGINT NOT NULL,
    CONSTRAINT pk_anime_streaming_availabilities PRIMARY KEY (id),
    CONSTRAINT uk_anime_streaming_availability_pair UNIQUE (anime_id, streaming_platform_id),
    CONSTRAINT fk_anime_streaming_availabilities_anime
        FOREIGN KEY (anime_id) REFERENCES animes (id),
    CONSTRAINT fk_anime_streaming_availabilities_streaming_platform
        FOREIGN KEY (streaming_platform_id) REFERENCES streaming_platforms (id)
);

CREATE TABLE anime_streaming_audio_languages (
    anime_streaming_availability_id BIGINT NOT NULL,
    language VARCHAR(80) NOT NULL,
    CONSTRAINT fk_anime_streaming_audio_languages_availability
        FOREIGN KEY (anime_streaming_availability_id) REFERENCES anime_streaming_availabilities (id)
);

CREATE TABLE anime_streaming_subtitle_languages (
    anime_streaming_availability_id BIGINT NOT NULL,
    language VARCHAR(80) NOT NULL,
    CONSTRAINT fk_anime_streaming_subtitle_languages_availability
        FOREIGN KEY (anime_streaming_availability_id) REFERENCES anime_streaming_availabilities (id)
);
