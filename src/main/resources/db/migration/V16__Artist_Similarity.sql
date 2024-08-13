CREATE TABLE artist_similarity (
    artist1 BIGINT NOT NULL,
    artist2 BIGINT NOT NULL,
    similarity DOUBLE NOT NULL,
    PRIMARY KEY (artist1, artist2),
    UNIQUE KEY unique_artist_pair (artist1, artist2),
    FOREIGN KEY (artist1) REFERENCES artist_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (artist2) REFERENCES artist_profiles(id) ON DELETE CASCADE
);


CREATE INDEX idx_similarity ON artist_similarity (similarity DESC);