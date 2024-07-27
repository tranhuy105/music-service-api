CREATE OR REPLACE VIEW track_details AS
SELECT t.id AS track_id,
       t.title AS track_title,
       t.duration AS track_duration,
       t.album_id,
       a.title AS album_title,
       a.cover_url AS album_cover_url,
       GROUP_CONCAT(aa.artist_id) AS artist_ids,
       GROUP_CONCAT(ap.stage_name) AS artist_stage_names,
       GROUP_CONCAT(ap.profile_picture_url) AS artist_profile_pictures,
       GROUP_CONCAT(aa.role) AS roles
FROM tracks t
JOIN albums a ON t.album_id = a.id
JOIN album_artists aa ON a.id = aa.album_id
JOIN artist_profiles ap ON aa.artist_id = ap.id
GROUP BY t.id;

CREATE OR REPLACE VIEW playlist_track_details AS
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
    GROUP_CONCAT(aa.artist_id) AS artist_ids,
    GROUP_CONCAT(ap.stage_name) AS artist_stage_names,
    GROUP_CONCAT(ap.profile_picture_url) AS artist_profile_pictures,
    GROUP_CONCAT(aa.role) AS roles
FROM playlist_track pt
JOIN tracks t ON pt.track_id = t.id
JOIN albums a ON t.album_id = a.id
JOIN album_artists aa ON a.id = aa.album_id
JOIN artist_profiles ap ON aa.artist_id = ap.id
GROUP BY pt.playlist_id, pt.position, pt.added_at, pt.added_by, pt.track_id, t.title, t.duration, a.id, a.title, a.cover_url;

CREATE INDEX idx_album_artists_album_id_artist_id ON album_artists (album_id, artist_id);
-- Query by playlist_id then order by these
CREATE INDEX idx_playlist_track_playlist_id_added_at ON playlist_track (playlist_id, added_at);
CREATE INDEX idx_playlist_track_playlist_id_position ON playlist_track (playlist_id, position);

-- Create full-text index for searching
ALTER TABLE tracks ADD FULLTEXT(title);
ALTER TABLE albums ADD FULLTEXT(title);
ALTER TABLE artist_profiles ADD FULLTEXT(stage_name);

