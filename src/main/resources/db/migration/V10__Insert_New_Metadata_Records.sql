INSERT INTO roles (id, name) VALUES (4, 'ROLE_ARTIST');

INSERT INTO user_role (user_id, role_id) VALUES
(1, 4),
(2, 4),
(3, 4),
(4, 4);

DELIMITER //

CREATE PROCEDURE createArtistProfile(
    IN p_user_id BIGINT,
    IN p_stage_name VARCHAR(100),
    IN p_bio TEXT,
    IN p_profile_picture_url VARCHAR(255)
)
BEGIN
    INSERT INTO user_role (user_id, role_id) VALUES (p_user_id,4);

    INSERT INTO artist_profiles (user_id, stage_name, bio, profile_picture_url)
    VALUES (p_user_id, p_stage_name, p_bio, p_profile_picture_url);
END //

CREATE PROCEDURE createAlbumWithArtists(
    IN p_title VARCHAR(100),
    IN p_release_date DATE,
    IN p_is_single BOOLEAN,
    IN p_cover_url VARCHAR(255),
    IN p_artist_roles JSON
)
BEGIN
    DECLARE album_id BIGINT;
    DECLARE i INT DEFAULT 0;
    DECLARE artist_id BIGINT;
    DECLARE role ENUM('main', 'support');
    DECLARE len INT;

    INSERT INTO albums (title, release_date, is_single, cover_url)
    VALUES (p_title, p_release_date, p_is_single, p_cover_url);

    SET album_id = LAST_INSERT_ID();

    SET len = JSON_LENGTH(p_artist_roles);
    WHILE i < len DO
        SET artist_id = JSON_UNQUOTE(JSON_EXTRACT(p_artist_roles, CONCAT('$[', i, '].artistId')));
        SET role = JSON_UNQUOTE(JSON_EXTRACT(p_artist_roles, CONCAT('$[', i, '].role')));

        INSERT INTO album_artists (album_id, artist_id, role)
        VALUES (album_id, artist_id, role);

        SET i = i + 1;
    END WHILE;
END //

CREATE PROCEDURE linkArtistToAlbum(
    IN p_album_id BIGINT,
    IN p_artist_id BIGINT,
    IN p_role ENUM('main', 'support')
)
BEGIN
    INSERT INTO album_artists (album_id, artist_id, role)
    VALUES (p_album_id, p_artist_id, p_role);
END //

CREATE PROCEDURE createTrack(
    IN p_album_id BIGINT,
    IN p_title VARCHAR(100),
    IN p_duration INT,
    OUT p_track_id BIGINT
)
BEGIN
    INSERT INTO tracks (album_id, title, duration)
    VALUES (p_album_id, p_title, p_duration);

    SET p_track_id = LAST_INSERT_ID();
END //

DELIMITER ;