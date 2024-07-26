-- Create playlist table
CREATE TABLE playlists (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  public BOOLEAN NOT NULL DEFAULT FALSE,
  cover_url VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create playlist_track join table
CREATE TABLE playlist_track (
  playlist_id BIGINT,
  track_id BIGINT,
  position INT NOT NULL,
  added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  added_by BIGINT,
  PRIMARY KEY (playlist_id, track_id),
  FOREIGN KEY (playlist_id) REFERENCES playlists(id),
  FOREIGN KEY (track_id) REFERENCES tracks(id),
  FOREIGN KEY (added_by) REFERENCES users(id),
  CONSTRAINT unique_position_per_playlist UNIQUE (playlist_id, position)
);

-- Create indexes
CREATE INDEX idx_playlists_user_id ON playlists (user_id);
CREATE INDEX idx_playlist_track_track_id ON playlist_track (track_id);
CREATE INDEX idx_playlist_track_playlist_id ON playlist_track (playlist_id);
CREATE INDEX idx_playlist_track_position ON playlist_track (position);
CREATE INDEX idx_playlist_track_added_at ON playlist_track (added_at);
CREATE INDEX idx_playlist_track_added_by ON playlist_track (added_by);

-- Adding view to get tracks of a playlist
CREATE VIEW playlist_track_details AS
SELECT
    pt.playlist_id AS playlist_id,
    pt.position,
    pt.added_at,
    pt.added_by,
    pt.track_id AS track_id,
    t.title AS track_title,
    t.duration AS track_duration,
    a.id AS album_id,
    a.title AS album_title,
    a.cover_url AS album_cover_url,
    ap.id AS artist_id,
    ap.stage_name AS artist_stage_name,
    ap.profile_picture_url AS artist_profile_picture_url,
    aa.role
FROM playlist_track pt
JOIN tracks t ON pt.track_id = t.id
JOIN albums a ON t.album_id = a.id
JOIN album_artists aa ON a.id = aa.album_id
JOIN artist_profiles ap ON aa.artist_id = ap.id;

-- Adding view to get playlist details and total track count
CREATE VIEW playlist_summary AS
SELECT
    p.id AS playlist_id,
    p.user_id AS user_id,
    p.name AS playlist_name,
    p.cover_url AS playlist_cover_url,
    p.description AS playlist_description,
    p.public AS is_public,
    COUNT(pt.track_id) AS total_tracks
FROM playlists p
LEFT JOIN playlist_track pt ON p.id = pt.playlist_id
GROUP BY p.id, p.name, p.cover_url;

-- Procedure for handling playlist position operation
DELIMITER //

CREATE PROCEDURE InsertPlaylistTrackAtEnd(
    IN p_playlist_id BIGINT,
    IN p_track_id BIGINT,
    IN p_added_by BIGINT
)
BEGIN
    DECLARE p_position BIGINT;

    -- Find the next position in the playlist
    SELECT COALESCE(MAX(position), 0) + 1 INTO p_position
    FROM playlist_track
    WHERE playlist_id = p_playlist_id;

    INSERT INTO playlist_track (playlist_id, track_id, position, added_by)
    VALUES (p_playlist_id, p_track_id, p_position, p_added_by);
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE DeletePlaylistTrack(
    IN p_playlist_id BIGINT,
    IN p_track_id BIGINT
)
BEGIN
    DELETE FROM playlist_track
    WHERE playlist_id = p_playlist_id AND track_id = p_track_id;

    UPDATE playlist_track
    SET position = position - 1
    WHERE playlist_id = p_playlist_id AND position > (
        SELECT position FROM playlist_track
        WHERE playlist_id = p_playlist_id AND track_id = p_track_id
    );
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE MovePlaylistTrack(
    IN p_playlist_id BIGINT,
    IN p_track_id BIGINT,
    IN p_new_position BIGINT
)
BEGIN
    DECLARE p_old_position BIGINT;

    SELECT position INTO p_old_position
    FROM playlist_track
    WHERE playlist_id = p_playlist_id AND track_id = p_track_id;

    IF p_old_position != p_new_position THEN
        UPDATE playlist_track
        SET position = NULL
        WHERE playlist_id = p_playlist_id AND track_id = p_track_id;

        IF p_old_position < p_new_position THEN
            UPDATE playlist_track
            SET position = position - 1
            WHERE playlist_id = p_playlist_id AND position > p_old_position AND position <= p_new_position;
        ELSE
            UPDATE playlist_track
            SET position = position + 1
            WHERE playlist_id = p_playlist_id AND position >= p_new_position AND position < p_old_position;
        END IF;

        UPDATE playlist_track
        SET position = p_new_position
        WHERE playlist_id = p_playlist_id AND track_id = p_track_id;
    END IF;
END //

DELIMITER ;


-- Insert Initial Data
INSERT INTO playlists (id, user_id, name, description, public, cover_url) VALUES
(1, 1, 'Rock Favorites', 'A collection of favorite rock tracks.', TRUE, 'https://example.com/rock_favorites_cover.jpg'),
(2, 1, 'Chill Vibes', 'Relaxing tracks to chill out.', TRUE, 'https://example.com/chill_vibes_cover.jpg'),
(3, 2, 'Epic Tracks', 'Tracks that are epic and memorable.', FALSE, 'https://example.com/epic_tracks_cover.jpg'),
(4, 2, 'Anime Hits', 'Popular tracks from various anime.', TRUE, 'https://example.com/anime_hits_cover.jpg'),
(5, 1, 'Band Collection', 'A playlist of all band tracks.', FALSE, 'https://example.com/band_collection_cover.jpg'),
(6, 3, 'My Empty Playlist', 'A playlist that currently has no tracks.', FALSE, null);

-- Insert tracks into 'Rock Favorites'
CALL InsertPlaylistTrackAtEnd(1, 1, 1);
CALL InsertPlaylistTrackAtEnd(1, 2, 1);
CALL InsertPlaylistTrackAtEnd(1, 4, 1);
CALL InsertPlaylistTrackAtEnd(1, 5, 1);

-- Insert tracks into 'Chill Vibes'
CALL InsertPlaylistTrackAtEnd(2, 3, 1);
CALL InsertPlaylistTrackAtEnd(2, 6, 1);
CALL InsertPlaylistTrackAtEnd(2, 7, 1);

-- Insert tracks into 'Epic Tracks'
CALL InsertPlaylistTrackAtEnd(3, 8, 2);
CALL InsertPlaylistTrackAtEnd(3, 10, 2);

-- Insert tracks into 'Anime Hits'
CALL InsertPlaylistTrackAtEnd(4, 9, 2);
CALL InsertPlaylistTrackAtEnd(4, 8, 2);

-- Insert tracks into 'Band Collection'
CALL InsertPlaylistTrackAtEnd(5, 1, 1);
CALL InsertPlaylistTrackAtEnd(5, 2, 1);
CALL InsertPlaylistTrackAtEnd(5, 3, 1);
CALL InsertPlaylistTrackAtEnd(5, 4, 1);
CALL InsertPlaylistTrackAtEnd(5, 5, 1);
CALL InsertPlaylistTrackAtEnd(5, 6, 1);
CALL InsertPlaylistTrackAtEnd(5, 7, 1);
CALL InsertPlaylistTrackAtEnd(5, 8, 1);
CALL InsertPlaylistTrackAtEnd(5, 10, 1);
