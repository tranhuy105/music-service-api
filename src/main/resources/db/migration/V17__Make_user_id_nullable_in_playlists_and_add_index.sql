ALTER TABLE track_genres ADD INDEX idx_genre_track (genre_id, track_id);
ALTER TABLE tracks ADD INDEX idx_stream_count (stream_count DESC);

ALTER TABLE playlists MODIFY user_id BIGINT NULL;
ALTER TABLE playlists ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
