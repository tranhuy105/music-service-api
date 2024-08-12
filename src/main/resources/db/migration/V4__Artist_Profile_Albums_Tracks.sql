-- Add view to get user details
CREATE VIEW user_details AS
SELECT u.id, u.firstname, u.lastname, u.dob, u.email, u.password, u.account_locked, u.enabled, r.id AS role_id, r.name AS role_name,
        IF(EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = u.id AND s.end_date > CURDATE()), TRUE, FALSE) AS is_premium
FROM users u
LEFT JOIN user_role ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id;

-- Create albumArtist profile table
CREATE TABLE artist_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    stage_name VARCHAR(100) NOT NULL,
    bio TEXT,
    profile_picture_url VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create albums table
CREATE TABLE albums (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    release_date DATE NOT NULL,
    is_single BOOLEAN NOT NULL DEFAULT FALSE,
    cover_url VARCHAR(255)
);

-- Create tracks table
CREATE TABLE tracks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT,
    title VARCHAR(100) NOT NULL,
    duration INT NOT NULL, -- in ms
    FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

-- Create album_artists join table with a role indicator
CREATE TABLE album_artists (
    album_id BIGINT,
    artist_id BIGINT,
    role ENUM('main', 'support') NOT NULL,
    PRIMARY KEY (album_id, artist_id),
    FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE,
    FOREIGN KEY (artist_id) REFERENCES artist_profiles(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_artist_profiles_user_id ON artist_profiles (user_id);
CREATE INDEX idx_tracks_album_id ON tracks (album_id);
CREATE INDEX idx_album_artists_album_id ON album_artists (album_id);
CREATE INDEX idx_album_artists_artist_id ON album_artists (artist_id);

-- Adding view to get albumArtist albums
CREATE VIEW artist_details AS
SELECT ap.id AS artist_id, stage_name AS artist_stage_name, bio AS artist_bio, profile_picture_url AS artist_profile_picture_url,
       aa.album_id, a.title AS album_title, a.release_date, a.is_single, a.cover_url, aa.role
FROM artist_profiles ap
JOIN album_artists aa ON ap.id = aa.artist_id
JOIN albums a ON aa.album_id = a.id;

-- Adding view to get tracks detail with album and albumArtist data
CREATE VIEW track_details AS
SELECT t.id AS track_id,
       t.title AS track_title,
       t.duration AS track_duration,
       t.album_id,
       a.title AS album_title,
       a.cover_url AS album_cover_url,
       aa.artist_id,
       ap.stage_name AS artist_stage_name,
       ap.profile_picture_url AS artist_profile_picture_url,
       aa.role
FROM tracks t
JOIN albums a ON t.album_id = a.id
JOIN album_artists aa ON a.id = aa.album_id
JOIN artist_profiles ap ON aa.artist_id = ap.id;

-- Adding view to get album details with all tracks and its albumArtist
CREATE VIEW album_details AS
SELECT a.id AS album_id, a.title AS album_title, a.release_date, a.is_single, a.cover_url,
       t.id AS track_id, t.title AS track_title, t.duration AS track_duration,
       ap.profile_picture_url AS artist_profile_picture_url, ap.stage_name AS artist_stage_name, ap.id AS artist_id, aa.role
FROM albums a
JOIN album_artists aa ON a.id = aa.album_id
JOIN artist_profiles ap ON aa.artist_id = ap.id
LEFT JOIN tracks t ON a.id = t.album_id;


-- Insert initial data
INSERT INTO artist_profiles (user_id, stage_name, bio, profile_picture_url)
VALUES
(1, '結束バンド', 'A fictional band from the anime Bocchi the Rock!', 'https://i.scdn.co/image/ab6761610000e5eb86de41539f26da7d0626e257'),
(2, 'Sangatsu No Phantasia', null, 'https://i.scdn.co/image/ab6761610000e5eb810a96f25d7658ce376dec6f'),
(3, 'Yoshino Nanjo', 'Japanese singer and actress known for her work in various anime.', 'https://example.com/yoshino_nanjo.jpg'),
(4, 'KanoeRana', 'Vocalist with a unique voice and style, contributing to several anime soundtracks.', 'https://example.com/kanoerana.jpg');

INSERT INTO albums (id, title, release_date, cover_url, is_single)
VALUES
(1, '結束バンド', '2022-12-28', 'https://i.scdn.co/image/ab67616d00001e0209ca036917527fa198ead7b1', false),
(2, '忘れてやらない', '2022-12-21', 'https://i.scdn.co/image/ab67616d00001e0209ca036917527fa198ead7b1', true),
(3, 'Pastel Rain', '2022-6-30', 'https://i.scdn.co/image/ab67616d0000b273cbfc898159e1321b03167f84', true),
(4, 'Misty Night', '2023-05-15', 'https://example.com/misty_night_cover.jpg', false),
(5, 'Eternal Harmony', '2023-01-30', 'https://example.com/eternal_harmony_cover.jpg', false);

INSERT INTO album_artists (album_id, artist_id, role)
VALUES
(1, 1, 'main'),
(2, 1, 'main'),
(3, 2, 'main'),
(4, 3, 'main'),
(4, 4, 'support'),
(5, 3, 'main'),
(5, 4, 'support');

INSERT INTO tracks (album_id, title, duration)
VALUES
(1, 'Seishun Complex', 232000),
(1, 'Into the light', 208000),
(1, 'Guitar, Loneliness and Blue Planet', 212000),
(1, 'Ano Band', 238000),
(4, 'Secret Base', 283000),
(2, 'Wasurete Yaranai', 223000),
(3, 'Pastel Rain', 230000),
(4, 'Twilight Breeze', 245000),
(4, 'Midnight Serenade', 260000),
(5, 'Harmony in Blue', 220000),
(5, 'Eternal Love', 230000);
