CREATE OR REPLACE VIEW album_details AS
SELECT a.id AS album_id, a.title AS album_title, a.release_date, a.is_single, a.cover_url,
       ap.profile_picture_url AS artist_profile_picture_url, ap.stage_name AS artist_stage_name, ap.id AS artist_id, aa.role
FROM albums a
JOIN album_artists aa ON a.id = aa.album_id
JOIN artist_profiles ap ON aa.artist_id = ap.id;