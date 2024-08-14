-- i forgot to add the CASCADE from the start lmao ☠️

ALTER TABLE playlist_track DROP FOREIGN KEY playlist_track_ibfk_1;
ALTER TABLE playlist_track DROP FOREIGN KEY playlist_track_ibfk_2;
ALTER TABLE playlist_track DROP FOREIGN KEY playlist_track_ibfk_3;


ALTER TABLE playlist_track
ADD CONSTRAINT playlist_track_ibfk_1
FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE;

ALTER TABLE playlist_track
ADD CONSTRAINT playlist_track_ibfk_2
FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE;

ALTER TABLE playlist_track
ADD CONSTRAINT playlist_track_ibfk_3
FOREIGN KEY (added_by) REFERENCES users(id) ON DELETE SET NULL;
