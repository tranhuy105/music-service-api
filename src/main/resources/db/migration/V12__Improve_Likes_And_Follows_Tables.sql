ALTER TABLE likes
    ADD COLUMN liked_at DATETIME DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_likes_user_id_added_at_desc ON likes (user_id, liked_at DESC);
CREATE INDEX idx_likes_user_id_added_at_asc ON likes (user_id, liked_at ASC);
CREATE INDEX idx_likes_added_at ON likes (liked_at);

CREATE OR REPLACE VIEW user_saved_tracks AS
SELECT
    l.user_id AS user_id,
    -1 AS position,
    l.user_id AS added_by,
    l.liked_at AS added_at,
    t.id AS track_id,
    t.title AS track_title,
    t.duration AS track_duration,
    t.stream_count,
    a.id AS album_id,
    a.title AS album_title,
    a.cover_url AS album_cover_url,
    GROUP_CONCAT(aa.artist_id) AS artist_ids,
    GROUP_CONCAT(ap.stage_name) AS artist_stage_names,
    GROUP_CONCAT(ap.profile_picture_url) AS artist_profile_pictures,
    GROUP_CONCAT(aa.role) AS roles
FROM likes l
JOIN tracks t ON l.track_id = t.id
JOIN albums a ON t.album_id = a.id
JOIN album_artists aa ON a.id = aa.album_id
JOIN artist_profiles ap ON aa.artist_id = ap.id
GROUP BY l.user_id, t.id, t.title, t.duration, t.stream_count, a.id, a.title, a.cover_url, l.liked_at;