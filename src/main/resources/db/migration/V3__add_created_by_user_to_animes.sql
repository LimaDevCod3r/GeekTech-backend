ALTER TABLE animes
    ADD COLUMN created_by_user_id BIGINT NOT NULL;

ALTER TABLE animes
    ADD CONSTRAINT fk_animes_created_by_user
    FOREIGN KEY (created_by_user_id) REFERENCES users (id);
