-- Create likes table
CREATE TABLE likes (
  user_id BIGINT,
  track_id BIGINT,
  PRIMARY KEY (user_id, track_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (track_id) REFERENCES tracks(id)
);

-- Create follows table
CREATE TABLE follows (
    user_id BIGINT,
    artist_profile_id BIGINT,
    PRIMARY KEY (user_id, artist_profile_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (artist_profile_id) REFERENCES artist_profiles(id)
);

CREATE INDEX idx_likes_user_id ON likes (user_id);
CREATE INDEX idx_likes_track_id ON likes (track_id);
CREATE INDEX idx_follows_user_id ON follows (user_id);
CREATE INDEX idx_follows_artist_profile_id ON follows (artist_profile_id);