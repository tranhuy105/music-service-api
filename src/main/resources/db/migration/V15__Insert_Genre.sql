DELIMITER //

CREATE PROCEDURE AssignRandomTrackGenres()
BEGIN
    SET @max_track_id = 529;
    SET @min_genre_id = 1;
    SET @max_genre_id = 49;

    SET @track_id = 1;
    WHILE @track_id <= @max_track_id DO
            SET @num_genres = FLOOR(1 + (RAND() * 5));
            SET @genre_count = 0;
            WHILE @genre_count < @num_genres DO
                    SET @genre_id = FLOOR(1 + (RAND() * (@max_genre_id - @min_genre_id)));
                    INSERT IGNORE INTO track_genres (track_id, genre_id)
                    VALUES (@track_id, @genre_id);
                    SET @genre_count = @genre_count + 1;
                END WHILE;
            SET @track_id = @track_id + 1;
        END WHILE;
END //

DELIMITER ;

INSERT INTO artist_genres (artist_id, genre_id)
VALUES
(1, 1),
(1, 17),
(1, 23),
(2, 1),
(2, 17),
(2, 23),
(5, 1),   -- Rock
(5, 17),  -- Alternative
(5, 23),  -- J-Pop
(6, 3),
(6, 11),
(7, 2),
(7, 8),
(7, 21),
(8, 2),
(8, 11),
(9, 2),
(9, 11),
(9, 13),
(9, 12),
(10, 2),
(10, 12),
(10, 17),
(11, 2),
(11, 7),
(11, 8),
(11, 23),
(12, 2),
(12, 12),
(12, 6),
(12, 17),
(13, 2),
(13, 16),
(13, 11),
(13, 17),
(14, 2),   -- Pop
(14, 11),  -- R&B
(14, 7),   -- Electronic
(14, 17),   -- Alternative
(15, 1),
(15, 40),
(15, 14),
(16, 1),
(16, 14),
(16, 6),
(16, 40),
(17, 2),
(17, 27),
(17, 12),
(17, 13),
(18, 27),
(18, 13),
(18, 12),
(19, 3),
(19, 31),
(19, 7),
(19, 11),
(20, 3),
(20, 31),
(20, 7),
(20, 11),
(21, 2),
(21, 17),
(21, 12),
(21, 23),
(22, 2),
(22, 17),
(22, 12),
(22, 23),
(23, 2),
(23, 17),
(23, 7),
(23, 23),
(24, 2),   -- Pop
(24, 17),  -- Alternative
(24, 1),   -- Rock
(24, 12),   -- Soul
(25, 2),
(25, 17),
(25, 7),
(25, 23),
(25, 1),
(26, 2),
(26, 7),
(26, 23),
(27, 1),
(27, 14),
(27, 39),
(28, 1),
(28, 14),
(28, 39),
(29, 4),
(29, 45),
(30, 4),
(30, 45),
(31, 4),
(31, 45),
(31, 46),
(31, 47),
(29, 46),
(30, 47),
(32, 4),
(32, 45),
(32, 47),
(33, 4),
(33, 45),
(33, 47),
(33, 46),
(34, 6),
(34, 4),
(35, 6),
(35, 1),
(35, 13),
(36, 6),
(36, 1),
(36, 13),
(37, 2),
(37, 7),
(37, 23);

CALL AssignRandomTrackGenres();
DROP PROCEDURE AssignRandomTrackGenres;


INSERT INTO likes (user_id, track_id) VALUE
(1, 2),
(1, 4),
(1, 7),
(1, 343),
(1, 345),
(1, 523),
(1, 528);
