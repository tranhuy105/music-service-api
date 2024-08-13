CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE track_genres (
    track_id BIGINT,
    genre_id BIGINT,
    PRIMARY KEY (track_id, genre_id),
    FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE artist_genres (
    artist_id BIGINT,
    genre_id BIGINT,
    PRIMARY KEY (artist_id, genre_id),
    FOREIGN KEY (artist_id) REFERENCES artist_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

INSERT INTO genres (name) VALUES ('Rock');
INSERT INTO genres (name) VALUES ('Pop');
INSERT INTO genres (name) VALUES ('Hip Hop');
INSERT INTO genres (name) VALUES ('Jazz');
INSERT INTO genres (name) VALUES ('Classical');
INSERT INTO genres (name) VALUES ('Blues');
INSERT INTO genres (name) VALUES ('Electronic');
INSERT INTO genres (name) VALUES ('Dance');
INSERT INTO genres (name) VALUES ('Reggae');
INSERT INTO genres (name) VALUES ('Country');
INSERT INTO genres (name) VALUES ('R&B');
INSERT INTO genres (name) VALUES ('Soul');
INSERT INTO genres (name) VALUES ('Funk');
INSERT INTO genres (name) VALUES ('Metal');
INSERT INTO genres (name) VALUES ('Punk');
INSERT INTO genres (name) VALUES ('Folk');
INSERT INTO genres (name) VALUES ('Alternative');
INSERT INTO genres (name) VALUES ('Indie');
INSERT INTO genres (name) VALUES ('Gospel');
INSERT INTO genres (name) VALUES ('Latin');
INSERT INTO genres (name) VALUES ('V-Pop');
INSERT INTO genres (name) VALUES ('US-UK');
INSERT INTO genres (name) VALUES ('J-Pop');
INSERT INTO genres (name) VALUES ('World Music');
INSERT INTO genres (name) VALUES ('Opera');
INSERT INTO genres (name) VALUES ('Ska');
INSERT INTO genres (name) VALUES ('Disco');
INSERT INTO genres (name) VALUES ('Techno');
INSERT INTO genres (name) VALUES ('House');
INSERT INTO genres (name) VALUES ('Trance');
INSERT INTO genres (name) VALUES ('Dubstep');
INSERT INTO genres (name) VALUES ('Drum and Bass');
INSERT INTO genres (name) VALUES ('Ambient');
INSERT INTO genres (name) VALUES ('New Age');
INSERT INTO genres (name) VALUES ('Soundtrack');
INSERT INTO genres (name) VALUES ('Synthwave');
INSERT INTO genres (name) VALUES ('Grunge');
INSERT INTO genres (name) VALUES ('Emo');
INSERT INTO genres (name) VALUES ('Hardcore');
INSERT INTO genres (name) VALUES ('Progressive Rock');
INSERT INTO genres (name) VALUES ('Experimental');
INSERT INTO genres (name) VALUES ('Lo-Fi');
INSERT INTO genres (name) VALUES ('Trip-Hop');
INSERT INTO genres (name) VALUES ('Industrial');
INSERT INTO genres (name) VALUES ('Vocal Jazz');
INSERT INTO genres (name) VALUES ('Bossa Nova');
INSERT INTO genres (name) VALUES ('Salsa');
INSERT INTO genres (name) VALUES ('Flamenco');
INSERT INTO genres (name) VALUES ('Bluegrass');
