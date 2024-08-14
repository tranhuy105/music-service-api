DELIMITER //

CREATE PROCEDURE CreateSystemPlaylist(
    IN p_name VARCHAR(100),
    IN p_description TEXT,
    IN p_cover_url VARCHAR(255),
    OUT p_playlist_id BIGINT
)
BEGIN
    INSERT INTO playlists (user_id, name, description, public, cover_url)
    VALUES (NULL, p_name, p_description, TRUE, p_cover_url);
    SET p_playlist_id = LAST_INSERT_ID();
END //

DELIMITER ;
