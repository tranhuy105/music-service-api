-- add new column to artist to track follower
ALTER TABLE artist_profiles
ADD COLUMN follower_count INT DEFAULT 0;

CREATE OR REPLACE VIEW artist_details AS
SELECT ap.id AS artist_id,
       stage_name AS artist_stage_name,
       bio AS artist_bio, profile_picture_url AS artist_profile_picture_url,
       follower_count AS artist_follower_count,
       aa.album_id, a.title AS album_title, a.release_date, a.is_single, a.cover_url, aa.role
FROM artist_profiles ap
JOIN album_artists aa ON ap.id = aa.artist_id
JOIN albums a ON aa.album_id = a.id;


-- add new column to tracks to track stream count
ALTER TABLE tracks ADD COLUMN stream_count BIGINT DEFAULT 0;

CREATE OR REPLACE VIEW track_details AS
SELECT t.id AS track_id,
       t.title AS track_title,
       t.duration AS track_duration,
       t.album_id,
       t.stream_count,
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
    t.stream_count,
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


-- ----------------------------------------------------------------------------------------------------------------------------------------
-- ----------------------------------------------------------------------------------------------------------------------------------------
-- NEW MOCK DATA
-- ----------------------------------------------------------------------------------------------------------------------------------------
-- ----------------------------------------------------------------------------------------------------------------------------------------

-- new procedure 😎😎 (for mock only)
DELIMITER //

CREATE PROCEDURE InsertArtistData(
    IN p_user_email VARCHAR(100),
    IN p_password VARCHAR(255),
    IN p_stage_name VARCHAR(100),
    IN p_bio TEXT,
    IN p_profile_picture_url VARCHAR(255),
    OUT p_artist_id BIGINT
)
BEGIN
    DECLARE v_user_id BIGINT;

    -- Create user
    INSERT INTO users (email, password, account_locked, enabled)
    VALUES (p_user_email, p_password, FALSE, TRUE);

    SET v_user_id = LAST_INSERT_ID();

    -- Assign ROLE_ARTIST
    INSERT INTO user_role (user_id, role_id)
    VALUES (v_user_id, 4),
           (v_user_id, 1);

    -- Create artist profile
    INSERT INTO artist_profiles (user_id, stage_name, bio, profile_picture_url)
    VALUES (v_user_id, p_stage_name, p_bio, p_profile_picture_url);

    SET p_artist_id = LAST_INSERT_ID();
END //

CREATE PROCEDURE CreateAlbumWithArtistsAndTracks(
    IN p_title VARCHAR(100),
    IN p_release_date DATE,
    IN p_is_single BOOLEAN,
    IN p_cover_url VARCHAR(255),
    IN p_artist_roles JSON,
    IN p_tracks JSON
)
BEGIN
    DECLARE v_album_id BIGINT;
    DECLARE i INT DEFAULT 0;
    DECLARE artist_id BIGINT;
    DECLARE role ENUM('main', 'support');
    DECLARE len INT;
    DECLARE track_id BIGINT;
    DECLARE track_title VARCHAR(100);
    DECLARE track_duration INT;

    -- Create album
    INSERT INTO albums (title, release_date, is_single, cover_url)
    VALUES (p_title, p_release_date, p_is_single, p_cover_url);

    SET v_album_id = LAST_INSERT_ID();

    -- Process artist roles
    SET len = JSON_LENGTH(p_artist_roles);
    WHILE i < len DO
            SET artist_id = JSON_UNQUOTE(JSON_EXTRACT(p_artist_roles, CONCAT('$[', i, '].artistId')));
            SET role = JSON_UNQUOTE(JSON_EXTRACT(p_artist_roles, CONCAT('$[', i, '].role')));

            INSERT INTO album_artists (album_id, artist_id, role)
            VALUES (v_album_id, artist_id, role);

            SET i = i + 1;
        END WHILE;

    -- Process tracks
    SET len = JSON_LENGTH(p_tracks);
    WHILE i < len DO
            SET track_title = JSON_UNQUOTE(JSON_EXTRACT(p_tracks, CONCAT('$[', i, '].title')));
            SET track_duration = JSON_UNQUOTE(JSON_EXTRACT(p_tracks, CONCAT('$[', i, '].duration')));

            CALL createTrack(v_album_id, track_title, track_duration, track_id);

            SET i = i + 1;
        END WHILE;
END //

DELIMITER ;


-- ヨルシカ mock data
SET @artist_id = NULL;
SET @secure_password = '$2a$10$/ixbgJIvnYpUCXR6sNw6FeMQuLfBuwdI1oHJWVk3hSGHMmczBcQai';
CALL InsertArtistData(
        'yorushika@example.com',
        @secure_password,
        'ヨルシカ',
        'ヨルシカ is a Japanese music project by composer and vocalist suis and guitarist and producer, n-buna.',
        'http://example.com/yorushika-profile.jpg',
    @artist_id);

CALL CreateAlbumWithArtistsAndTracks(
        'だから僕は音楽を辞めた',
        '2018-11-07',
        FALSE,
        'https://example.com/album1-cover.jpg',
        CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
        '[{"title": "だから僕は音楽を辞めた", "duration": 234000}, {"title": "言って。", "duration": 243000}, {"title": "花瓶に触れた", "duration": 242000}, {"title": "おかえり", "duration": 256000}, {"title": "藍", "duration": 245000}, {"title": "ノスタルジア", "duration": 248000}, {"title": "雲雀", "duration": 256000}, {"title": "雨とカプチーノ", "duration": 250000}]'
    );
CALL CreateAlbumWithArtistsAndTracks(
        '月光',
        '2019-07-03',
        FALSE,
        'https://example.com/album2-cover.jpg',
        CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
        '[{"title": "月光", "duration": 252000}, {"title": "心の底から", "duration": 236000}, {"title": "声", "duration": 223000}, {"title": "セツナ", "duration": 262000}, {"title": "セプテンバー", "duration": 251000}, {"title": "会いたい", "duration": 234000}, {"title": "新しい世界", "duration": 246000}, {"title": "青", "duration": 241000}]'
    );
CALL CreateAlbumWithArtistsAndTracks(
        'エルマ',
        '2021-07-21',
        FALSE,
        'https://example.com/album3-cover.jpg',
        CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
        '[{"title": "エルマ", "duration": 235000}, {"title": "ララバイ", "duration": 245000}, {"title": "空に", "duration": 241000}, {"title": "道", "duration": 250000}, {"title": "出会い", "duration": 242000}, {"title": "さよなら", "duration": 243000}, {"title": "夢", "duration": 255000}, {"title": "君と僕", "duration": 238000}]'
    );

-- Insert Snoop Dogg artist data
SET @snoop_artist_id = NULL;
CALL InsertArtistData(
    'snoopdogg@example.com',
    @secure_password,
    'Snoop Dogg',
    'Snoop Dogg is an American rapper, singer, and actor known for his impact on hip-hop and popular music.',
    'http://example.com/snoopdogg-profile.jpg',
    @snoop_artist_id
);

-- Album 1: Doggystyle
CALL CreateAlbumWithArtistsAndTracks(
    'Doggystyle',
    '1993-11-23',
    FALSE,
    'https://example.com/doggystyle-cover.jpg',
    CONCAT('[{"artistId": ', @snoop_artist_id, ', "role": "main"}]'),
    '[{"title": "Doggystyle", "duration": 268000}, {"title": "Gin and Juice", "duration": 231000}, {"title": "Murder Was the Case", "duration": 269000}, {"title": "Nuthin'' But a G Thang", "duration": 240000}, {"title": "Who Am I? (What''s My Name?)", "duration": 232000}]'
);

-- Album 2: No Limit Top Dogg
CALL CreateAlbumWithArtistsAndTracks(
    'No Limit Top Dogg',
    '1999-11-23',
    FALSE,
    'https://example.com/no-limit-top-dogg-cover.jpg',
    CONCAT('[{"artistId": ', @snoop_artist_id, ', "role": "main"}]'),
    '[{"title": "No Limit Top Dogg", "duration": 254000}, {"title": "Snoop Dogg (What''s My Name Pt. 2)", "duration": 228000}, {"title": "Woof!", "duration": 238000}, {"title": "We Just Wanna Party With You", "duration": 247000}, {"title": "Payback", "duration": 266000}]'
);


-- son tung mtp
CALL InsertArtistData(
    'sontungmtp@example.com',
    @secure_password,
    'Sơn Tùng M-TP',
    'Sơn Tùng M-TP is a popular Vietnamese singer and songwriter known for his unique blend of pop, R&B, and Vietnamese music.',
    'http://example.com/sontungmtp-profile.jpg',
    @artist_id
);

-- Album: MT-P
CALL CreateAlbumWithArtistsAndTracks(
    'MT-P',
    '2022-08-10',
    FALSE,
    'https://example.com/album4-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "MT-P", "duration": 275000}, {"title": "Em của ngày hôm qua", "duration": 250000}, {"title": "Âm Thầm Bên Em", "duration": 240000}, {"title": "Nắng ấm xa dần", "duration": 210000}, {"title": "Chúng Ta Không Thuộc Về Nhau", "duration": 320000}]'
);

-- Single 1: Hãy Trao Cho Anh (feat. Snoop Dogg)
CALL CreateAlbumWithArtistsAndTracks(
    'Hãy Trao Cho Anh (feat. Snoop Dogg)',
    '2019-07-01',
    TRUE,
    'https://example.com/single1-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}, {"artistId": ', @snoop_artist_id, ', "role": "support"}]'),
    '[{"title": "Hãy Trao Cho Anh", "duration": 275000}]'
);

-- Single 2: Chạy Ngay Đi
CALL CreateAlbumWithArtistsAndTracks(
    'Chạy Ngay Đi',
    '2018-03-01',
    TRUE,
    'https://example.com/single2-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Chạy Ngay Đi", "duration": 210000}]'
);

-- Single 3: Lạc Trôi
CALL CreateAlbumWithArtistsAndTracks(
    'Lạc Trôi',
    '2017-01-06',
    TRUE,
    'https://example.com/single3-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Lạc Trôi", "duration": 250000}]'
);

-- Single 4: Âm Thầm Bên Em
CALL CreateAlbumWithArtistsAndTracks(
    'Âm Thầm Bên Em',
    '2015-09-24',
    TRUE,
    'https://example.com/single4-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Âm Thầm Bên Em", "duration": 240000}]'
);



-- Justin Bieber
SET @justin_id = NULL;
CALL InsertArtistData(
    'justinbieber@example.com',
    @secure_password,
    'Justin Bieber',
    'Justin Bieber is a Canadian singer and songwriter who gained fame as a teenager with hits like "Baby" and "Sorry".',
    'http://example.com/justinbieber-profile.jpg',
    @justin_id
);

-- Album: Purpose
CALL CreateAlbumWithArtistsAndTracks(
    'Purpose',
    '2015-11-13',
    FALSE,
    'https://example.com/purpose-cover.jpg',
    CONCAT('[{"artistId": ', @justin_id, ', "role": "main"}]'),
    '[{"title": "What Do You Mean?", "duration": 215000}, {"title": "Sorry", "duration": 203000}, {"title": "Love Yourself", "duration": 233000}, {"title": "Company", "duration": 221000}, {"title": "No Pressure", "duration": 216000}]'
);

-- Album: Believe
CALL CreateAlbumWithArtistsAndTracks(
    'Believe',
    '2012-06-15',
    FALSE,
    'https://example.com/believe-cover.jpg',
    CONCAT('[{"artistId": ', @justin_id, ', "role": "main"}]'),
    '[{"title": "Boyfriend", "duration": 207000}, {"title": "As Long As You Love Me", "duration": 223000}, {"title": "Beauty and a Beat", "duration": 225000}, {"title": "All Around the World", "duration": 226000}, {"title": "Die in Your Arms", "duration": 207000}]'
);

-- Album: Changes
CALL CreateAlbumWithArtistsAndTracks(
    'Changes',
    '2020-02-14',
    FALSE,
    'https://example.com/changes-cover.jpg',
    CONCAT('[{"artistId": ', @justin_id, ', "role": "main"}]'),
    '[{"title": "Yummy", "duration": 209000}, {"title": "Intentions", "duration": 203000}, {"title": "Forever", "duration": 208000}, {"title": "Available", "duration": 201000}, {"title": "Come Around Me", "duration": 206000}]'
);



-- Bruno Mars
SET @bruno_id = NULL;
CALL InsertArtistData(
    'brunomars@example.com',
    @secure_password,
    'Bruno Mars',
    'Bruno Mars is an American singer, songwriter, record producer, and performer known for his hit songs and energetic performances.',
    'http://example.com/brunomars-profile.jpg',
    @bruno_id
);

-- Album: Doo-Wops & Hooligans
CALL CreateAlbumWithArtistsAndTracks(
    'Doo-Wops & Hooligans',
    '2010-10-05',
    FALSE,
    'https://example.com/doowops-hooligans-cover.jpg',
    CONCAT('[{"artistId": ', @bruno_id, ', "role": "main"}]'),
    '[{"title": "Just the Way You Are", "duration": 215000}, {"title": "Grenade", "duration": 212000}, {"title": "The Lazy Song", "duration": 224000}, {"title": "Marry You", "duration": 208000}, {"title": "Count on Me", "duration": 220000}]'
);

-- Album: Unorthodox Jukebox
CALL CreateAlbumWithArtistsAndTracks(
    'Unorthodox Jukebox',
    '2012-12-07',
    FALSE,
    'https://example.com/unorthodox-jukebox-cover.jpg',
    CONCAT('[{"artistId": ', @bruno_id, ', "role": "main"}]'),
    '[{"title": "Locked Out of Heaven", "duration": 221000}, {"title": "When I Was Your Man", "duration": 215000}, {"title": "Treasure", "duration": 207000}, {"title": "Show Me", "duration": 209000}, {"title": "Moonshine", "duration": 212000}]'
);

-- Album: 24K Magic
CALL CreateAlbumWithArtistsAndTracks(
    '24K Magic',
    '2016-11-18',
    FALSE,
    'https://example.com/24k-magic-cover.jpg',
    CONCAT('[{"artistId": ', @bruno_id, ', "role": "main"}]'),
    '[{"title": "24K Magic", "duration": 230000}, {"title": "That’s What I Like", "duration": 220000}, {"title": "Versace on the Floor", "duration": 235000}, {"title": "Finesse", "duration": 214000}, {"title": "Perm", "duration": 217000}]'
);

-- Fuji Kaze
SET @fuji_id = NULL;
CALL InsertArtistData(
        'fujikaze@example.com',
        @secure_password,
        '藤井 風',
        '藤井 風は、日本のシンガーソングライターで、感情豊かなパフォーマンスと独自の音楽スタイルで知られています。',
        'http://example.com/fujikaze-profile.jpg',
        @fuji_id
    );

-- Album: HELP EVER HURT NEVER
CALL CreateAlbumWithArtistsAndTracks(
        'HELP EVER HURT NEVER',
        '2020-10-07',
        FALSE,
        'https://example.com/help-ever-hurt-never-cover.jpg',
        CONCAT('[{"artistId": ', @fuji_id, ', "role": "main"}]'),
        '[{"title": "ナンナン", "duration": 223000}, {"title": "恋", "duration": 212000}, {"title": "死ぬのがいいわ", "duration": 238000}, {"title": "もえ", "duration": 215000}, {"title": "ヘデモ", "duration": 220000}]'
    );

-- Album: STARS
CALL CreateAlbumWithArtistsAndTracks(
        'STARS',
        '2021-05-07',
        FALSE,
        'https://example.com/stars-cover.jpg',
        CONCAT('[{"artistId": ', @fuji_id, ', "role": "main"}]'),
        '[{"title": "風", "duration": 230000}, {"title": "雨", "duration": 205000}, {"title": "祭り", "duration": 210000}, {"title": "秘密", "duration": 220000}, {"title": "メリー", "duration": 212000}]'
    );

-- Album: LOVE ALL SERVE ALL
CALL CreateAlbumWithArtistsAndTracks(
        'LOVE ALL SERVE ALL',
        '2022-04-07',
        FALSE,
        'https://example.com/love-all-serve-all-cover.jpg',
        CONCAT('[{"artistId": ', @fuji_id, ', "role": "main"}]'),
        '[{"title": "好き", "duration": 235000}, {"title": "たこ焼き", "duration": 220000}, {"title": "夜", "duration": 230000}, {"title": "あなた", "duration": 215000}, {"title": "未来", "duration": 220000}]'
    );


CALL InsertArtistData(
    'deco27@example.com',
    @secure_password,
    'DECO*27',
    'DECO*27 is a Japanese music producer known for his vocaloid works and distinctive style.',
    'http://example.com/deco27-profile.jpg',
    @artist_id
);

-- DECO*27 Album: ゴースト (Ghost) (2020)
CALL CreateAlbumWithArtistsAndTracks(
    'ゴースト',
    '2020-09-09',
    FALSE,
    'https://example.com/ghost-2020-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "ゴースト", "duration": 248000}, {"title": "ジョーカー", "duration": 227000}, {"title": "スポットライト", "duration": 236000}, {"title": "バンパイア", "duration": 251000}, {"title": "光", "duration": 265000}, {"title": "運命", "duration": 244000}]'
);

-- DECO*27 Album: 桜 (Sakura) (2022)
CALL CreateAlbumWithArtistsAndTracks(
    '桜',
    '2022-03-23',
    FALSE,
    'https://example.com/sakura-2022-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "桜", "duration": 263000}, {"title": "地平線", "duration": 248000}, {"title": "ワルツ", "duration": 242000}, {"title": "ノスタルジア", "duration": 251000}, {"title": "逃走", "duration": 260000}, {"title": "約束", "duration": 253000}]'
);

-- Add single ラビットホール (Rabbit Hole) by DECO*27
CALL CreateAlbumWithArtistsAndTracks(
    'ラビットホール',
    '2023-05-12',
    TRUE,
    'https://example.com/rabbit-hole-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "ラビットホール", "duration": 247000}]'
);

-- Create Adele
SET @artist_id = NULL;
CALL InsertArtistData(
    'adele@example.com',
    @secure_password,
    'Adele',
    'Adele Laurie Blue Adkins is a British singer-songwriter known for her powerful voice and emotive songs.',
    'http://example.com/adele-profile.jpg',
    @artist_id
);

-- Adele Albums
CALL CreateAlbumWithArtistsAndTracks(
    '25',
    '2015-11-20',
    FALSE,
    'https://example.com/25-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Hello", "duration": 295000}, {"title": "Send My Love (To Your New Lover)", "duration": 226000}, {"title": "I Miss You", "duration": 238000}, {"title": "When We Were Young", "duration": 261000}, {"title": "Remedy", "duration": 274000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    '30',
    '2021-11-19',
    FALSE,
    'https://example.com/30-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Easy On Me", "duration": 250000}, {"title": "Oh My God", "duration": 226000}, {"title": "Can I Get It", "duration": 215000}, {"title": "I Drink Wine", "duration": 280000}, {"title": "My Little Love", "duration": 247000}]'
);

-- Adele Single
CALL CreateAlbumWithArtistsAndTracks(
    'Easy On Me',
    '2021-10-15',
    TRUE,
    'https://example.com/easy-on-me-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Easy On Me", "duration": 250000}]'
);

-- Create Ed Sheeran
SET @artist_id = NULL;
CALL InsertArtistData(
    'edsheeran@example.com',
    @secure_password,
    'Ed Sheeran',
    'Edward Christopher Sheeran is an English singer-songwriter known for his mix of folk, pop, and acoustic music.',
    'http://example.com/ed-sheeran-profile.jpg',
    @artist_id
);

-- Ed Sheeran Albums
CALL CreateAlbumWithArtistsAndTracks(
    '÷ (Divide)',
    '2017-03-03',
    FALSE,
    'https://example.com/divide-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Shape of You", "duration": 233000}, {"title": "Castle on the Hill", "duration": 262000}, {"title": "Galway Girl", "duration": 226000}, {"title": "Happier", "duration": 212000}, {"title": "Perfect", "duration": 263000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    '= (Equals)',
    '2021-10-29',
    FALSE,
    'https://example.com/equals-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Bad Habits", "duration": 220000}, {"title": "Shivers", "duration": 237000}, {"title": "Overpass Graffiti", "duration": 237000}, {"title": "The Joker and the Queen", "duration": 233000}, {"title": "Visiting Hours", "duration": 258000}]'
);

-- Ed Sheeran Single
CALL CreateAlbumWithArtistsAndTracks(
    'Bad Habits',
    '2021-06-25',
    TRUE,
    'https://example.com/bad-habits-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Bad Habits", "duration": 220000}]'
);

-- Create The Weeknd
SET @artist_id = NULL;
CALL InsertArtistData(
    'theweeknd@example.com',
    @secure_password,
    'The Weeknd',
    'Abel Makkonen Tesfaye, known as The Weeknd, is a Canadian singer, songwriter, and record producer.',
    'http://example.com/the-weeknd-profile.jpg',
    @artist_id
);

-- The Weeknd Albums
CALL CreateAlbumWithArtistsAndTracks(
    'After Hours',
    '2020-03-20',
    FALSE,
    'https://example.com/after-hours-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Blinding Lights", "duration": 200000}, {"title": "In Your Eyes", "duration": 222000}, {"title": "Save Your Tears", "duration": 219000}, {"title": "Heartless", "duration": 217000}, {"title": "Too Late", "duration": 235000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Starboy',
    '2016-11-25',
    FALSE,
    'https://example.com/starboy-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Starboy", "duration": 207000}, {"title": "I Feel It Coming", "duration": 208000}, {"title": "Party Monster", "duration": 229000}, {"title": "Reminder", "duration": 223000}, {"title": "Secrets", "duration": 215000}]'
);

-- The Weeknd Single
CALL CreateAlbumWithArtistsAndTracks(
    'Blinding Lights',
    '2019-11-29',
    TRUE,
    'https://example.com/blinding-lights-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Blinding Lights", "duration": 200000}]'
);


-- Create Queen
SET @artist_id = NULL;
CALL InsertArtistData(
    'queen@example.com',
    @secure_password,
    'Queen',
    'Queen is a British rock band known for their eclectic musical style and powerful performances.',
    'http://example.com/queen-profile.jpg',
    @artist_id
);

-- Queen Albums
CALL CreateAlbumWithArtistsAndTracks(
    'A Night at the Opera',
    '1975-11-21',
    FALSE,
    'https://example.com/a-night-at-the-opera-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Death on Two Legs (Dedicated to...)", "duration": 283000}, {"title": "Lazing on a Sunday Afternoon", "duration": 102000}, {"title": "I''m in Love with My Car", "duration": 193000}, {"title": "You''re My Best Friend", "duration": 174000}, {"title": "39", "duration": 220000}, {"title": "Sweet Lady", "duration": 212000}, {"title": "Seaside Rendezvous", "duration": 163000}, {"title": "The Prophet''s Song", "duration": 375000}, {"title": "Love of My Life", "duration": 210000}, {"title": "Good Company", "duration": 227000}, {"title": "Bohemian Rhapsody", "duration": 354000}, {"title": "God Save the Queen", "duration": 125000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'News of the World',
    '1977-10-28',
    FALSE,
    'https://example.com/news-of-the-world-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "We Will Rock You", "duration": 123000}, {"title": "We Are the Champions", "duration": 179000}, {"title": "Sheer Heart Attack", "duration": 160000}, {"title": "All Dead, All Dead", "duration": 226000}, {"title": "Spread Your Wings", "duration": 195000}, {"title": "Fight from the Inside", "duration": 211000}, {"title": "Get Down, Make Love", "duration": 200000}, {"title": "Sleeping on the Sidewalk", "duration": 210000}, {"title": "Who Needs You", "duration": 146000}, {"title": "It''s Late", "duration": 284000}, {"title": "My Melancholy Blues", "duration": 133000}]'
);

-- Queen Single
CALL CreateAlbumWithArtistsAndTracks(
    'Bohemian Rhapsody',
    '1975-10-31',
    TRUE,
    'https://example.com/bohemian-rhapsody-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Bohemian Rhapsody", "duration": 354000}]'
);

-- Create Led Zeppelin
SET @artist_id = NULL;
CALL InsertArtistData(
    'ledzeppelin@example.com',
    @secure_password,
    'Led Zeppelin',
    'Led Zeppelin was an influential British rock band known for its powerful music and iconic sound.',
    'http://example.com/led-zeppelin-profile.jpg',
    @artist_id
);

-- Led Zeppelin Albums
CALL CreateAlbumWithArtistsAndTracks(
    'Led Zeppelin IV',
    '1971-11-08',
    FALSE,
    'https://example.com/led-zeppelin-iv-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Black Dog", "duration": 295000}, {"title": "Rock and Roll", "duration": 215000}, {"title": "The Battle of Evermore", "duration": 235000}, {"title": "Stairway to Heaven", "duration": 482000}, {"title": "Misty Mountain Hop", "duration": 220000}, {"title": "Four Sticks", "duration": 265000}, {"title": "Going to California", "duration": 212000}, {"title": "When the Levee Breaks", "duration": 431000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Physical Graffiti',
    '1975-02-24',
    FALSE,
    'https://example.com/physical-graffiti-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Custard Pie", "duration": 216000}, {"title": "The Rover", "duration": 245000}, {"title": "In My Time of Dying", "duration": 369000}, {"title": "Houses of the Holy", "duration": 275000}, {"title": "Trampled Under Foot", "duration": 269000}, {"title": "Kashmir", "duration": 354000}, {"title": "In the Light", "duration": 300000}, {"title": "Bron-Yr-Aur", "duration": 160000}, {"title": "Down by the Seaside", "duration": 236000}, {"title": "Ten Years Gone", "duration": 370000}, {"title": "Night Flight", "duration": 188000}, {"title": "The Wanton Song", "duration": 252000}, {"title": "Boogie with Stu", "duration": 179000}, {"title": "Black Country Woman", "duration": 187000}, {"title": "Sick Again", "duration": 232000}]'
);

-- Led Zeppelin Single
CALL CreateAlbumWithArtistsAndTracks(
    'Stairway to Heaven',
    '1971-11-08',
    TRUE,
    'https://example.com/stairway-to-heaven-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Stairway to Heaven", "duration": 482000}]'
);

-- Create Bee Gees
SET @artist_id = NULL;
CALL InsertArtistData(
    'beegees@example.com',
    @secure_password,
    'Bee Gees',
    'The Bee Gees were a British-Australian band known for their contributions to the disco era and pop music.',
    'http://example.com/bee-gees-profile.jpg',
    @artist_id
);

-- Bee Gees Albums
CALL CreateAlbumWithArtistsAndTracks(
    'Saturday Night Fever',
    '1977-11-15',
    FALSE,
    'https://example.com/saturday-night-fever-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Stayin'' Alive", "duration": 214000}, {"title": "How Deep Is Your Love", "duration": 231000}, {"title": "Night Fever", "duration": 259000}, {"title": "More Than a Woman", "duration": 179000}, {"title": "If I Can''t Have You", "duration": 178000}, {"title": "A Fifth of Beethoven", "duration": 207000}, {"title": "More Than a Woman", "duration": 221000}, {"title": "Manhattan Skyline", "duration": 250000}, {"title": "The Lord''s Prayer", "duration": 150000}, {"title": "Disco Inferno", "duration": 293000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Spirits Having Flown',
    '1979-02-05',
    FALSE,
    'https://example.com/spirits-having-flown-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Tragedy", "duration": 265000}, {"title": "Too Much Heaven", "duration": 274000}, {"title": "Love You Inside Out", "duration": 212000}, {"title": "Reaching Out", "duration": 262000}, {"title": "Spirits (Having Flown)", "duration": 235000}, {"title": "Stop (Think Again)", "duration": 192000}, {"title": "Never Say Never Again", "duration": 248000}, {"title": "Search, Find", "duration": 202000}, {"title": "Sailing", "duration": 235000}, {"title": "Nothing Could Be Good", "duration": 253000}]'
);

-- Bee Gees Single
CALL CreateAlbumWithArtistsAndTracks(
    'Stayin\' Alive',
    '1977-11-15',
    TRUE,
    'https://example.com/stayin-alive-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Stayin'' Alive", "duration": 214000}]'
);

-- Create Chic
SET @artist_id = NULL;
CALL InsertArtistData(
    'chic@example.com',
    @secure_password,
    'Chic',
    'Chic was a prominent American band known for its influential disco hits in the late 70s and early 80s.',
    'http://example.com/chic-profile.jpg',
    @artist_id
);

-- Chic Albums
CALL CreateAlbumWithArtistsAndTracks(
    'C\'est Chic',
    '1978-07-21',
    FALSE,
    'https://example.com/cest-chic-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Le Freak", "duration": 306000}, {"title": "I Want Your Love", "duration": 236000}, {"title": "I Lost My Bag", "duration": 195000}, {"title": "Chic Cheer", "duration": 216000}, {"title": "Savoir Faire", "duration": 247000}, {"title": "Happy Man", "duration": 218000}, {"title": "At Last I Am Free", "duration": 260000}, {"title": "Look At Me, Look At You", "duration": 183000}, {"title": "L''étoile", "duration": 257000}, {"title": "The Greatest Thing", "duration": 200000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Risqué',
    '1979-10-01',
    FALSE,
    'https://example.com/risque-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Good Times", "duration": 282000}, {"title": "My Forbidden Lover", "duration": 226000}, {"title": "Can''t Stand to Love You", "duration": 216000}, {"title": "Will You Cry", "duration": 238000}, {"title": "My Feet Keep Dancing", "duration": 222000}, {"title": "I Was Made to Love Her", "duration": 226000}, {"title": "The Greatest Thing", "duration": 212000}, {"title": "What About Me", "duration": 250000}, {"title": "Sorry, I", "duration": 233000}, {"title": "Why Not", "duration": 194000}]'
);

-- Chic Single
CALL CreateAlbumWithArtistsAndTracks(
    'Le Freak',
    '1978-07-21',
    TRUE,
    'https://example.com/le-freak-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id, ', "role": "main"}]'),
    '[{"title": "Le Freak", "duration": 306000}]'
);


-- Create Travis Scott
SET @artist_id_travis = NULL;
CALL InsertArtistData(
    'travis.scott@example.com',
    @secure_password,
    'Travis Scott',
    'Travis Scott is an American rapper, singer, songwriter, and record producer known for his unique sound blending hip hop, psychedelic, and ambient elements.',
    'http://example.com/travis-scott-profile.jpg',
    @artist_id_travis
);

-- Create Quavo
SET @artist_id_quavo = NULL;
CALL InsertArtistData(
    'quavo@example.com',
    @secure_password,
    'Quavo',
    'Quavo is an American rapper, singer, and songwriter, best known as a member of the group Migos.',
    'http://example.com/quavo-profile.jpg',
    @artist_id_quavo
);

-- Create Albums and Tracks for Travis Scott
CALL CreateAlbumWithArtistsAndTracks(
    'Astroworld',
    '2018-08-03',
    FALSE,
    'https://example.com/astroworld-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id_travis, ', "role": "main"}]'),
    '[{"title": "STARGAZING", "duration": 210000}, {"title": "CAROUSEL", "duration": 170000}, {"title": "R.I.P. SCREW", "duration": 180000}, {"title": "SKELETONS", "duration": 200000}, {"title": "WHO? WHAT!", "duration": 195000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Rodeo',
    '2015-09-04',
    FALSE,
    'https://example.com/rodeo-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id_travis, ', "role": "main"}]'),
    '[{"title": "ANTIDOTE", "duration": 230000}, {"title": "3500", "duration": 250000}, {"title": "WASTED", "duration": 165000}, {"title": "PISCES", "duration": 175000}, {"title": "BUTTERFLY EFFECT", "duration": 176000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'HIGHEST IN THE ROOM',
    '2019-10-04',
    TRUE,
    'https://example.com/highest-in-the-room-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id_travis, ', "role": "main"}]'),
    '[{"title": "HIGHEST IN THE ROOM", "duration": 175000}]'
);

-- Create Albums and Tracks for Quavo
CALL CreateAlbumWithArtistsAndTracks(
    'Quavo Huncho',
    '2018-10-12',
    FALSE,
    'https://example.com/quavo-huncho-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id_quavo, ', "role": "main"}]'),
    '[{"title": "WORKIN ME", "duration": 175000}, {"title": "HOTEL LOBBY", "duration": 180000}, {"title": "BIGGEST ALLEY", "duration": 190000}, {"title": "PASS OUT", "duration": 170000}, {"title": "TOO MUCH SAUCE", "duration": 185000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Culture III',
    '2021-06-11',
    FALSE,
    'https://example.com/culture-iii-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id_quavo, ', "role": "main"}]'),
    '[{"title": "AVENGE", "duration": 220000}, {"title": "ROAD RUNNER", "duration": 195000}, {"title": "PICK UP THE PHONE", "duration": 200000}, {"title": "BET ON ME", "duration": 200000}, {"title": "MOTION", "duration": 185000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'LAMB TALK',
    '2019-01-22',
    TRUE,
    'https://example.com/lamb-talk-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id_quavo, ', "role": "main"}]'),
    '[{"title": "LAMB TALK", "duration": 180000}]'
);

-- Create Collaborative Album
CALL CreateAlbumWithArtistsAndTracks(
    'Huncho Jack, Jack Huncho',
    '2017-12-22',
    FALSE,
    'https://example.com/huncho-jack-jack-huncho-cover.jpg',
    CONCAT('[{"artistId": ', @artist_id_travis, ', "role": "main"}, {"artistId": ', @artist_id_quavo, ', "role": "main"}]'),
    '[{"title": "Modern Slavery", "duration": 161000}, {"title": "Mamacita", "duration": 185000}, {"title": "Dubai Shit", "duration": 173000}, {"title": "Go", "duration": 170000}, {"title": "How U Feel", "duration": 169000}]'
);

-- Insert Aimer
SET @aimer_id = NULL;
CALL InsertArtistData(
    'aimer@example.com',
    @secure_password,
    'Aimer',
    'Aimerは、アニメテーマや感情豊かなボーカルで知られる日本のシンガーです。',
    'http://example.com/aimer-profile.jpg',
    @aimer_id
);

-- Insert Aimer's Albums and Tracks
CALL CreateAlbumWithArtistsAndTracks(
    'Penny Rain',
    '2018-04-04',
    FALSE,
    'https://example.com/penny-rain-cover.jpg',
    CONCAT('[{"artistId": ', @aimer_id, ', "role": "main"}]'),
    '[{"title": "I Beg You", "duration": 274000}, {"title": "Black Bird", "duration": 251000}, {"title": "Kokoro", "duration": 273000}, {"title": "Waltz", "duration": 260000}, {"title": "恋はあと恋", "duration": 276000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'daydream',
    '2016-12-21',
    FALSE,
    'https://example.com/daydream-cover.jpg',
    CONCAT('[{"artistId": ', @aimer_id, ', "role": "main"}]'),
    '[{"title": "Ref:rain", "duration": 315000}, {"title": "恋はあと恋", "duration": 227000}, {"title": "Starlight", "duration": 248000}, {"title": "Rain", "duration": 242000}, {"title": "花の歌", "duration": 225000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Sleepless Nights',
    '2014-11-05',
    FALSE,
    'https://example.com/sleepless-nights-cover.jpg',
    CONCAT('[{"artistId": ', @aimer_id, ', "role": "main"}]'),
    '[{"title": "Brave Shine", "duration": 255000}, {"title": "恋は歌", "duration": 236000}, {"title": "六等星の夜", "duration": 272000}, {"title": "誰かの夜", "duration": 248000}, {"title": "見つめ", "duration": 244000}]'
);

-- Insert Kenshi Yonezu
SET @yonezu_id = NULL;
CALL InsertArtistData(
    'kenshi.yonezu@example.com',
    @secure_password,
    '米津玄師',
    '米津玄師は、多様な音楽スタイルとアニメテーマで知られる人気の日本のシンガーソングライターです。',
    'http://example.com/yonezu-profile.jpg',
    @yonezu_id
);

-- Insert Kenshi Yonezu's Albums and Tracks
CALL CreateAlbumWithArtistsAndTracks(
    'STRAY SHEEP',
    '2020-08-05',
    FALSE,
    'https://example.com/stray-sheep-cover.jpg',
    CONCAT('[{"artistId": ', @yonezu_id, ', "role": "main"}]'),
    '[{"title": "Lemon", "duration": 257000}, {"title": "馬と鹿", "duration": 243000}, {"title": "Pale Blue", "duration": 232000}, {"title": "カムパネルラ", "duration": 232000}, {"title": "一緒に", "duration": 232000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Bremen',
    '2015-11-18',
    FALSE,
    'https://example.com/bremen-cover.jpg',
    CONCAT('[{"artistId": ', @yonezu_id, ', "role": "main"}]'),
    '[{"title": "LOSER", "duration": 226000}, {"title": "アイデア", "duration": 254000}, {"title": "フロー", "duration": 262000}, {"title": "ピースサイン", "duration": 238000}, {"title": "君の名は", "duration": 249000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'YANKEE',
    '2014-03-05',
    FALSE,
    'https://example.com/yankee-cover.jpg',
    CONCAT('[{"artistId": ', @yonezu_id, ', "role": "main"}]'),
    '[{"title": "オリオン座", "duration": 219000}, {"title": "サンタマリア", "duration": 234000}, {"title": "スカート", "duration": 224000}, {"title": "ファミリーマート", "duration": 236000}, {"title": "グリーン・アイズ", "duration": 239000}]'
);

-- Insert Eve
SET @eve_id = NULL;
CALL InsertArtistData(
    'eve@example.com',
    @secure_password,
    'Eve',
    'Eveは、独自のスタイルとアニメ関連の楽曲で知られる日本のシンガーソングライター兼音楽プロデューサーです。',
    'http://example.com/eve-profile.jpg',
    @eve_id
);

-- Insert Eve's Albums and Tracks
CALL CreateAlbumWithArtistsAndTracks(
    'Smile',
    '2020-07-22',
    FALSE,
    'https://example.com/smile-cover.jpg',
    CONCAT('[{"artistId": ', @eve_id, ', "role": "main"}]'),
    '[{"title": "Smile", "duration": 232000}, {"title": "Dramaturgy", "duration": 241000}, {"title": "怪物", "duration": 259000}, {"title": "星屑", "duration": 267000}, {"title": "僕らの恋", "duration": 255000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Buster',
    '2021-01-20',
    FALSE,
    'https://example.com/buster-cover.jpg',
    CONCAT('[{"artistId": ', @eve_id, ', "role": "main"}]'),
    '[{"title": "Buster", "duration": 239000}, {"title": "栄", "duration": 228000}, {"title": "心", "duration": 246000}, {"title": "恋の行方", "duration": 244000}, {"title": "青い", "duration": 235000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Riot',
    '2019-05-22',
    FALSE,
    'https://example.com/riot-cover.jpg',
    CONCAT('[{"artistId": ', @eve_id, ', "role": "main"}]'),
    '[{"title": "Riot", "duration": 230000}, {"title": "怪物", "duration": 262000}, {"title": "極光", "duration": 241000}, {"title": "もっと", "duration": 255000}, {"title": "ビター", "duration": 238000}]'
);

-- Insert King Gnu
SET @kinggnu_id = NULL;
CALL InsertArtistData(
    'kinggnu@example.com',
    @secure_password,
    'King Gnu',
    'King Gnuは、ロック、ポップ、ジャズを融合させた音楽で知られる日本のバンドです。',
    'http://example.com/kinggnu-profile.jpg',
    @kinggnu_id
);

-- Insert King Gnu's Albums and Tracks
CALL CreateAlbumWithArtistsAndTracks(
    'CEREMONY',
    '2020-03-04',
    FALSE,
    'https://example.com/ceremony-cover.jpg',
    CONCAT('[{"artistId": ', @kinggnu_id, ', "role": "main"}]'),
    '[{"title": "白日", "duration": 297000}, {"title": "Teenager Forever", "duration": 271000}, {"title": "傘", "duration": 244000}, {"title": "Jiggy", "duration": 228000}, {"title": "Hakujitsu", "duration": 292000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Sympa',
    '2019-01-16',
    FALSE,
    'https://example.com/sympa-cover.jpg',
    CONCAT('[{"artistId": ', @kinggnu_id, ', "role": "main"}]'),
    '[{"title": "Hitman", "duration": 275000}, {"title": "Flash!!!", "duration": 265000}, {"title": "まさか", "duration": 249000}, {"title": "The hole", "duration": 262000}, {"title": "Prayer X", "duration": 288000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'W',
    '2017-11-15',
    FALSE,
    'https://example.com/w-cover.jpg',
    CONCAT('[{"artistId": ', @kinggnu_id, ', "role": "main"}]'),
    '[{"title": "サマーレイン", "duration": 257000}, {"title": "ランプ", "duration": 229000}, {"title": "パプリカ", "duration": 248000}, {"title": "フルーツ", "duration": 242000}, {"title": "ない", "duration": 266000}]'
);

-- Insert YOASOBI
SET @yoasobi_id = NULL;
CALL InsertArtistData(
    'yoasobi@example.com',
    @secure_password,
    'YOASOBI',
    'YOASOBIは、日本の音楽ユニットで、アニメとのコラボレーションでも知られています。彼らの音楽は、ストーリー性のある歌詞とキャッチーなメロディーが特徴です。',
    'http://example.com/yoasobi-profile.jpg',
    @yoasobi_id
);

-- Insert YOASOBI's Albums and Tracks
CALL CreateAlbumWithArtistsAndTracks(
    'THE BOOK',
    '2021-01-06',
    FALSE,
    'https://example.com/the-book-cover.jpg',
    CONCAT('[{"artistId": ', @yoasobi_id, ', "role": "main"}]'),
    '[{"title": "夜に駆ける", "duration": 204000}, {"title": "あの夢をなぞって", "duration": 232000}, {"title": "ハルジオン", "duration": 220000}, {"title": "怪物", "duration": 265000}, {"title": "青春病", "duration": 248000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'E-SIDE',
    '2021-11-17',
    FALSE,
    'https://example.com/e-side-cover.jpg',
    CONCAT('[{"artistId": ', @yoasobi_id, ', "role": "main"}]'),
    '[{"title": "三原色", "duration": 207000}, {"title": "ジョウネツノバイセップス", "duration": 222000}, {"title": "イエスタデイ", "duration": 232000}, {"title": "上弦の月", "duration": 240000}, {"title": "たぶん", "duration": 215000}]'
);

-- SUISEIIIIIIIIIIIIIIIIIIIIIIIII
SET @suisei_id = NULL;
CALL InsertArtistData(
    'suisei@example.com',
    @secure_password,
    '星街すいせい',
    '星街すいせいは、日本のVTuberであり、歌手としても知られています。彼女の音楽は、アニメやポップカルチャーの影響を受けた明るくキャッチーなものです。',
    'http://example.com/suisei-profile.jpg',
    @suisei_id
);

-- Insert Hoshimachi Suisei's Albums and Tracks
CALL CreateAlbumWithArtistsAndTracks(
    'Stellar Stellar',
    '2021-01-06',
    FALSE,
    'https://example.com/stellar-stellar-cover.jpg',
    CONCAT('[{"artistId": ', @suisei_id, ', "role": "main"}]'),
    '[{"title": "Stellar Stellar", "duration": 241000}, {"title": "Next Color Planet", "duration": 220000}, {"title": "キミと僕のメリークリスマス", "duration": 258000}, {"title": "Genius", "duration": 235000}, {"title": "ぼっち", "duration": 254000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'IDOL',
    '2020-09-09',
    FALSE,
    'https://example.com/idol-cover.jpg',
    CONCAT('[{"artistId": ', @suisei_id, ', "role": "main"}]'),
    '[{"title": "IDOL", "duration": 234000}, {"title": "シンクロニシティ", "duration": 246000}, {"title": "Sweet Devil", "duration": 211000}, {"title": "天球", "duration": 263000}, {"title": "10%", "duration": 242000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'LOST EDEN',
    '2022-01-12',
    FALSE,
    'https://example.com/lost-eden-cover.jpg',
    CONCAT('[{"artistId": ', @suisei_id, ', "role": "main"}]'),
    '[{"title": "LOST EDEN", "duration": 250000}, {"title": "Cosmos", "duration": 229000}, {"title": "Brave", "duration": 212000}, {"title": "Aurora", "duration": 245000}, {"title": "Shining", "duration": 258000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Non-Fiction',
    '2021-09-01',
    FALSE,
    'https://example.com/non-fiction-cover.jpg',
    CONCAT('[{"artistId": ', @suisei_id, ', "role": "main"}]'),
    '[{"title": "Non-Fiction", "duration": 245000}, {"title": "フルパワー", "duration": 233000}, {"title": "日常", "duration": 222000}, {"title": "ブレイカー", "duration": 244000}, {"title": "Diva", "duration": 257000}]'
);

-- Single: "Last Song"
CALL CreateAlbumWithArtistsAndTracks(
    'Last Song',
    '2022-06-15',
    TRUE,
    'https://example.com/last-song-cover.jpg',
    CONCAT('[{"artistId": ', @suisei_id, ', "role": "main"}]'),
    '[{"title": "Last Song", "duration": 270000}]'
);

-- Single: "Go! Go! Go!"
CALL CreateAlbumWithArtistsAndTracks(
    'Go! Go! Go!',
    '2023-03-12',
    TRUE,
    'https://example.com/go-go-go-cover.jpg',
    CONCAT('[{"artistId": ', @suisei_id, ', "role": "main"}]'),
    '[{"title": "Go! Go! Go!", "duration": 230000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'デデビダ',
    '2024-03-21',
    TRUE,
    'https://example.com/dedebida-cover.jpg',
    CONCAT('[{"artistId": ', @suisei_id, ', "role": "main"}]'),
    '[{"title": "デデビダ", "duration": 245000}]'
);


-- Insert Metallica artist
CALL InsertArtistData(
    'metallica@example.com',
    @secure_password,
    'Metallica',
    'Metallica is an American heavy metal band formed in 1981. They are known for their aggressive sound and fast tempos.',
    'http://example.com/metallica-profile.jpg',
    @metallica_id
);

-- Albums
CALL CreateAlbumWithArtistsAndTracks(
    'Master of Puppets',
    '1986-03-03',
    FALSE,
    'https://example.com/master-of-puppets-cover.jpg',
    CONCAT('[{"artistId": ', @metallica_id, ', "role": "main"}]'),
    '[{"title": "Battery", "duration": 259000}, {"title": "Master of Puppets", "duration": 515000}, {"title": "The Thing That Should Not Be", "duration": 394000}, {"title": "Welcome Home (Sanitarium)", "duration": 321000}, {"title": "Disposable Heroes", "duration": 336000}, {"title": "Leper Messiah", "duration": 279000}, {"title": "Orion", "duration": 324000}, {"title": "Damage, Inc.", "duration": 275000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    '...And Justice for All',
    '1988-09-07',
    FALSE,
    'https://example.com/justice-cover.jpg',
    CONCAT('[{"artistId": ', @metallica_id, ', "role": "main"}]'),
    '[{"title": "Blackened", "duration": 295000}, {"title": "...And Justice for All", "duration": 408000}, {"title": "Eye of the Beholder", "duration": 322000}, {"title": "One", "duration": 262000}, {"title": "The Shortest Straw", "duration": 310000}, {"title": "Harvester of Sorrow", "duration": 320000}, {"title": "The Frayed Ends of Sanity", "duration": 315000}, {"title": "To Live Is to Die", "duration": 316000}, {"title": "Dyers Eve", "duration": 223000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'The Black Album',
    '1991-08-12',
    FALSE,
    'https://example.com/black-album-cover.jpg',
    CONCAT('[{"artistId": ', @metallica_id, ', "role": "main"}]'),
    '[{"title": "Enter Sandman", "duration": 331000}, {"title": "Sad But True", "duration": 319000}, {"title": "Holier Than Thou", "duration": 260000}, {"title": "The Unforgiven", "duration": 381000}, {"title": "Never Neverland", "duration": 230000}, {"title": "Through the Never", "duration": 320000}, {"title": "Nothing Else Matters", "duration": 388000}, {"title": "Of Wolf and Man", "duration": 258000}, {"title": "The God That Failed", "duration": 267000}, {"title": "My Friend of Misery", "duration": 274000}, {"title": "The Struggle Within", "duration": 250000}]'
);

-- Singles
CALL CreateAlbumWithArtistsAndTracks(
    'One',
    '1989-03-20',
    TRUE,
    'https://example.com/one-single-cover.jpg',
    CONCAT('[{"artistId": ', @metallica_id, ', "role": "main"}]'),
    '[{"title": "One", "duration": 262000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Enter Sandman',
    '1991-08-20',
    TRUE,
    'https://example.com/enter-sandman-single-cover.jpg',
    CONCAT('[{"artistId": ', @metallica_id, ', "role": "main"}]'),
    '[{"title": "Enter Sandman", "duration": 331000}]'
);

-- Insert Megadeth artist
CALL InsertArtistData(
    'megadeth@example.com',
    @secure_password,
    'Megadeth',
    'Megadeth is an American thrash metal band formed in 1983, known for their fast tempo and complex arrangements.',
    'http://example.com/megadeth-profile.jpg',
    @megadeth_id
);

-- Albums
CALL CreateAlbumWithArtistsAndTracks(
    'Peace Sells... But Who\'s Buying?',
    '1986-09-19',
    FALSE,
    'https://example.com/peace-sells-cover.jpg',
    CONCAT('[{"artistId": ', @megadeth_id, ', "role": "main"}]'),
    '[{"title": "Wake Up Dead", "duration": 292000}, {"title": "The Conjuring", "duration": 209000}, {"title": "Peace Sells", "duration": 322000}, {"title": "Devils Island", "duration": 327000}, {"title": "Good Mourning/Black Friday", "duration": 309000}, {"title": "Bad Omen", "duration": 263000}, {"title": "I Ain''t Superstitious", "duration": 226000}, {"title": "My Last Words", "duration": 279000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Rust in Peace',
    '1990-09-24',
    FALSE,
    'https://example.com/rust-in-peace-cover.jpg',
    CONCAT('[{"artistId": ', @megadeth_id, ', "role": "main"}]'),
    '[{"title": "Holy Wars... The Punishment Due", "duration": 396000}, {"title": "Hangar 18", "duration": 291000}, {"title": "Take No Prisoners", "duration": 275000}, {"title": "Five Magics", "duration": 326000}, {"title": "Poison Was the Cure", "duration": 223000}, {"title": "Lucretia", "duration": 248000}, {"title": "Tornado of Souls", "duration": 304000}, {"title": "Dawn Patrol", "duration": 155000}, {"title": "Rust in Peace... Polaris", "duration": 277000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Countdown to Extinction',
    '1992-07-14',
    FALSE,
    'https://example.com/countdown-to-extinction-cover.jpg',
    CONCAT('[{"artistId": ', @megadeth_id, ', "role": "main"}]'),
    '[{"title": "Skin o'' My Teeth", "duration": 219000}, {"title": "Symphony of Destruction", "duration": 238000}, {"title": "Architecture of Aggression", "duration": 243000}, {"title": "Foreclosure of a Dream", "duration": 252000}, {"title": "This Was My Life", "duration": 260000}, {"title": "Countdown to Extinction", "duration": 294000}, {"title": "High Speed Dirt", "duration": 267000}, {"title": "Psychotron", "duration": 300000}, {"title": "Captive Honor", "duration": 289000}, {"title": "Ashes in Your Mouth", "duration": 281000}]'
);

-- Singles
CALL CreateAlbumWithArtistsAndTracks(
    'Hangar 18',
    '1990-06-04',
    TRUE,
    'https://example.com/hangar-18-single-cover.jpg',
    CONCAT('[{"artistId": ', @megadeth_id, ', "role": "main"}]'),
    '[{"title": "Hangar 18", "duration": 291000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Symphony of Destruction',
    '1992-06-01',
    TRUE,
    'https://example.com/symphony-of-destruction-single-cover.jpg',
    CONCAT('[{"artistId": ', @megadeth_id, ', "role": "main"}]'),
    '[{"title": "Symphony of Destruction", "duration": 238000}]'
);

-- Insert Miles Davis artist
CALL InsertArtistData(
    'milesdavis@example.com',
    @secure_password,
    'Miles Davis',
    'Miles Davis was an American jazz trumpeter, bandleader, and composer. He is widely considered one of the most influential musicians of the 20th century.',
    'http://example.com/miles-davis-profile.jpg',
    @miles_davis_id
);

-- Albums for Miles Davis
CALL CreateAlbumWithArtistsAndTracks(
    'Kind of Blue',
    '1959-08-17',
    FALSE,
    'https://example.com/kind-of-blue-cover.jpg',
    CONCAT('[{"artistId": ', @miles_davis_id, ', "role": "main"}]'),
    '[{"title": "So What", "duration": 322000}, {"title": "Freddie Freeloader", "duration": 338000}, {"title": "Blue in Green", "duration": 337000}, {"title": "All Blues", "duration": 693000}, {"title": "Flamenco Sketches", "duration": 567000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Bitches Brew',
    '1970-03-30',
    FALSE,
    'https://example.com/bitches-brew-cover.jpg',
    CONCAT('[{"artistId": ', @miles_davis_id, ', "role": "main"}]'),
    '[{"title": "Pharaoh’s Dance", "duration": 12012000}, {"title": "Bitches Brew", "duration": 1617000}, {"title": "Spanish Key", "duration": 10662000}, {"title": "John McLaughlin", "duration": 290000}, {"title": "Miles Runs the Voodoo Down", "duration": 8410000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Sketches of Spain',
    '1960-03-21',
    FALSE,
    'https://example.com/sketches-of-spain-cover.jpg',
    CONCAT('[{"artistId": ', @miles_davis_id, ', "role": "main"}]'),
    '[{"title": "Concierto de Aranjuez (Adagio)", "duration": 979000}, {"title": "Will O’ the Wisp", "duration": 178000}, {"title": "The Pan Piper", "duration": 286000}, {"title": "Saeta", "duration": 327000}, {"title": "Soleá", "duration": 414000}]'
);

-- Insert John Coltrane artist
CALL InsertArtistData(
    'johncoltrane@example.com',
    @secure_password,
    'John Coltrane',
    'John Coltrane was an American jazz saxophonist and composer, known for his pioneering work in modal jazz and his influence on the genre.',
    'http://example.com/john-coltrane-profile.jpg',
    @john_coltrane_id
);

-- Albums for John Coltrane
CALL CreateAlbumWithArtistsAndTracks(
    'A Love Supreme',
    '1965-01-01',
    FALSE,
    'https://example.com/a-love-supreme-cover.jpg',
    CONCAT('[{"artistId": ', @john_coltrane_id, ', "role": "main"}]'),
    '[{"title": "Acknowledgement", "duration": 286000}, {"title": "Resolution", "duration": 162000}, {"title": "Pursuance", "duration": 456000}, {"title": "Psalm", "duration": 332000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Giant Steps',
    '1960-01-01',
    FALSE,
    'https://example.com/giant-steps-cover.jpg',
    CONCAT('[{"artistId": ', @john_coltrane_id, ', "role": "main"}]'),
    '[{"title": "Giant Steps", "duration": 285000}, {"title": "Cousin Mary", "duration": 352000}, {"title": "Countdown", "duration": 147000}, {"title": "Spiral", "duration": 280000}, {"title": "Syeeda’s Song Flute", "duration": 238000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'My Favorite Things',
    '1960-01-01',
    FALSE,
    'https://example.com/my-favorite-things-cover.jpg',
    CONCAT('[{"artistId": ', @john_coltrane_id, ', "role": "main"}]'),
    '[{"title": "My Favorite Things", "duration": 826000}, {"title": "Everytime We Say Goodbye", "duration": 356000}, {"title": "Summertime", "duration": 369000}, {"title": "But Not for Me", "duration": 381000}]'
);

-- Insert Louis Armstrong artist
CALL InsertArtistData(
    'louisarmstrong@example.com',
    @secure_password,
    'Louis Armstrong',
    'Louis Armstrong was an American trumpeter, composer, and vocalist. He was one of the most influential figures in jazz music.',
    'http://example.com/louis-armstrong-profile.jpg',
    @louis_armstrong_id
);

-- Albums for Louis Armstrong
CALL CreateAlbumWithArtistsAndTracks(
    'What a Wonderful World',
    '1967-10-01',
    FALSE,
    'https://example.com/what-a-wonderful-world-cover.jpg',
    CONCAT('[{"artistId": ', @louis_armstrong_id, ', "role": "main"}]'),
    '[{"title": "What a Wonderful World", "duration": 139000}, {"title": "Cabaret", "duration": 175000}, {"title": "A Kiss to Build a Dream On", "duration": 185000}, {"title": "When It''s Sleepy Time Down South", "duration": 205000}, {"title": "The Bucket''s Got a Hole in It", "duration": 185000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Satchmo at Symphony Hall',
    '1951-01-01',
    FALSE,
    'https://example.com/satchmo-at-symphony-hall-cover.jpg',
    CONCAT('[{"artistId": ', @louis_armstrong_id, ', "role": "main"}]'),
    '[{"title": "Stardust", "duration": 245000}, {"title": "When It''s Sleepy Time Down South", "duration": 230000}, {"title": "The World Is Waiting for the Sunrise", "duration": 180000}, {"title": "Basin Street Blues", "duration": 220000}, {"title": "Saint Louis Blues", "duration": 255000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Ella and Louis',
    '1956-01-01',
    FALSE,
    'https://example.com/ella-and-louis-cover.jpg',
    CONCAT('[{"artistId": ', @louis_armstrong_id, ', "role": "main"}]'),
    '[{"title": "Can''t We Be Friends", "duration": 182000}, {"title": "Moonlight in Vermont", "duration": 201000}, {"title": "Cheek to Cheek", "duration": 190000}, {"title": "They Can''t Take That Away from Me", "duration": 180000}, {"title": "Tenderly", "duration": 189000}]'
);

-- Insert Ella Fitzgerald artist
CALL InsertArtistData(
    'ellafitzgerald@example.com',
    @secure_password,
    'Ella Fitzgerald',
    'Ella Fitzgerald was an American jazz singer, known as the "First Lady of Song" for her exceptional vocal range and improvisational skills.',
    'http://example.com/ella-fitzgerald-profile.jpg',
    @ella_fitzgerald_id
);

-- Albums for Ella Fitzgerald
CALL CreateAlbumWithArtistsAndTracks(
    'Ella Fitzgerald Sings the Cole Porter Songbook',
    '1956-01-01',
    FALSE,
    'https://example.com/ella-fitzgerald-sings-cole-porter-songbook-cover.jpg',
    CONCAT('[{"artistId": ', @ella_fitzgerald_id, ', "role": "main"}]'),
    '[{"title": "Anything Goes", "duration": 172000}, {"title": "I Get a Kick Out of You", "duration": 176000}, {"title": "Just One of Those Things", "duration": 169000}, {"title": "You Do Something to Me", "duration": 165000}, {"title": "Night and Day", "duration": 163000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Ella and Louis',
    '1956-01-01',
    FALSE,
    'https://example.com/ella-and-louis-cover.jpg',
    CONCAT('[{"artistId": ', @ella_fitzgerald_id, ', "role": "main"}]'),
    '[{"title": "Can''t We Be Friends", "duration": 182000}, {"title": "Moonlight in Vermont", "duration": 201000}, {"title": "Cheek to Cheek", "duration": 190000}, {"title": "They Can''t Take That Away from Me", "duration": 180000}, {"title": "Tenderly", "duration": 189000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Ella Fitzgerald Sings the Duke Ellington Songbook',
    '1957-01-01',
    FALSE,
    'https://example.com/ella-fitzgerald-sings-duke-ellington-songbook-cover.jpg',
    CONCAT('[{"artistId": ', @ella_fitzgerald_id, ', "role": "main"}]'),
    '[{"title": "It Don''t Mean a Thing (If It Ain''t Got That Swing)", "duration": 192000}, {"title": "Mood Indigo", "duration": 210000}, {"title": "I Ain''t Got Nothin'' But the Blues", "duration": 178000}, {"title": "Sophisticated Lady", "duration": 258000}, {"title": "Prelude to a Kiss", "duration": 185000}]'
);

-- Insert Duke Ellington artist
CALL InsertArtistData(
    'dukeellington@example.com',
    @secure_password,
    'Duke Ellington',
    'Duke Ellington was an American composer, pianist, and bandleader of jazz orchestras. He is considered one of the most influential figures in jazz history.',
    'http://example.com/duke-ellington-profile.jpg',
    @duke_ellington_id
);

-- Albums for Duke Ellington
CALL CreateAlbumWithArtistsAndTracks(
    'Ellington at Newport',
    '1956-01-01',
    FALSE,
    'https://example.com/ellington-at-newport-cover.jpg',
    CONCAT('[{"artistId": ', @duke_ellington_id, ', "role": "main"}]'),
    '[{"title": "Diminuendo in Blue", "duration": 316000}, {"title": "Crescendo in Blue", "duration": 439000}, {"title": "The Duke", "duration": 155000}, {"title": "Jeep''s Blues", "duration": 212000}, {"title": "Ko-Ko", "duration": 175000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Masterpieces by Ellington',
    '1951-01-01',
    FALSE,
    'https://example.com/masterpieces-by-ellington-cover.jpg',
    CONCAT('[{"artistId": ', @duke_ellington_id, ', "role": "main"}]'),
    '[{"title": "The Tattooed Bride", "duration": 380000}, {"title": "The Far East Suite", "duration": 526000}, {"title": "The Gilded Circle", "duration": 255000}, {"title": "The Mooche", "duration": 238000}, {"title": "Take the ''A'' Train", "duration": 178000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Far East Suite',
    '1966-01-01',
    FALSE,
    'https://example.com/far-east-suite-cover.jpg',
    CONCAT('[{"artistId": ', @duke_ellington_id, ', "role": "main"}]'),
    '[{"title": "Far East Suite", "duration": 603000}, {"title": "Ad Lib on Nippon", "duration": 457000}, {"title": "Depk", "duration": 345000}, {"title": "Bluebird of Delhi", "duration": 294000}, {"title": "Kinda Dukish", "duration": 379000}]'
);

-- Insert B.B. King artist
CALL InsertArtistData(
    'bbking@example.com',
    @secure_password,
    'B.B. King',
    'B.B. King was an American blues guitarist and singer, known for his expressive string bends and vibrato technique.',
    'http://example.com/bb-king-profile.jpg',
    @bb_king_id
);

-- Albums for B.B. King
CALL CreateAlbumWithArtistsAndTracks(
    'Live at the Regal',
    '1965-12-09',
    FALSE,
    'https://example.com/live-at-the-regal-cover.jpg',
    CONCAT('[{"artistId": ', @bb_king_id, ', "role": "main"}]'),
    '[{"title": "Every Day I Have the Blues", "duration": 175000}, {"title": "Sweet Little Angel", "duration": 302000}, {"title": "It''s My Own Fault Darlin''", "duration": 198000}, {"title": "How Blue Can You Get", "duration": 234000}, {"title": "Worry, Worry", "duration": 260000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'The Thrill Is Gone',
    '1969-08-01',
    FALSE,
    'https://example.com/the-thrill-is-gone-cover.jpg',
    CONCAT('[{"artistId": ', @bb_king_id, ', "role": "main"}]'),
    '[{"title": "The Thrill Is Gone", "duration": 323000}, {"title": "Sweet Little Sixteen", "duration": 197000}, {"title": "Please Accept My Love", "duration": 155000}, {"title": "Hummingbird", "duration": 258000}, {"title": "I''ve Always Been Crazy", "duration": 201000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Blues on the Bayou',
    '1998-10-27',
    FALSE,
    'https://example.com/blues-on-the-bayou-cover.jpg',
    CONCAT('[{"artistId": ', @bb_king_id, ', "role": "main"}]'),
    '[{"title": "Blues on the Bayou", "duration": 256000}, {"title": "I Like to Live the Love", "duration": 233000}, {"title": "Paying the Cost to Be the Boss", "duration": 282000}, {"title": "A Good Man Is Hard to Find", "duration": 214000}, {"title": "All Over Again", "duration": 266000}]'
);

-- Insert Stevie Ray Vaughan artist
CALL InsertArtistData(
    'stevierayvaughan@example.com',
    @secure_password,
    'Stevie Ray Vaughan',
    'Stevie Ray Vaughan was an American blues rock guitarist, singer, and songwriter, known for his fiery guitar work and passionate vocals.',
    'http://example.com/stevie-ray-vaughan-profile.jpg',
    @stevie_ray_vaughan_id
);

-- Albums for Stevie Ray Vaughan
CALL CreateAlbumWithArtistsAndTracks(
    'Texas Flood',
    '1983-06-13',
    FALSE,
    'https://example.com/texas-flood-cover.jpg',
    CONCAT('[{"artistId": ', @stevie_ray_vaughan_id, ', "role": "main"}]'),
    '[{"title": "Love Struck Baby", "duration": 141000}, {"title": "Pride and Joy", "duration": 220000}, {"title": "Texas Flood", "duration": 178000}, {"title": "Tell Me", "duration": 227000}, {"title": "Rude Mood", "duration": 140000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Couldn’t Stand the Weather',
    '1984-05-15',
    FALSE,
    'https://example.com/couldnt-stand-the-weather-cover.jpg',
    CONCAT('[{"artistId": ', @stevie_ray_vaughan_id, ', "role": "main"}]'),
    '[{"title": "Scuttle Buttin''", "duration": 119000}, {"title": "Couldn’t Stand the Weather", "duration": 294000}, {"title": "The Things (That) I Used to Do", "duration": 262000}, {"title": "Come on (Part III)", "duration": 248000}, {"title": "Voodoo Child (Slight Return)", "duration": 417000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'In Step',
    '1989-06-06',
    FALSE,
    'https://example.com/in-step-cover.jpg',
    CONCAT('[{"artistId": ', @stevie_ray_vaughan_id, ', "role": "main"}]'),
    '[{"title": "Crossfire", "duration": 255000}, {"title": "Tightrope", "duration": 260000}, {"title": "Let Me Love You Baby", "duration": 245000}, {"title": "Wall of Denial", "duration": 257000}, {"title": "Leave My Girl Alone", "duration": 251000}]'
);

-- Insert Buddy Guy artist
CALL InsertArtistData(
    'buddyguy@example.com',
    @secure_password,
    'Buddy Guy',
    'Buddy Guy is an American blues guitarist and singer, recognized for his expressive and powerful guitar style and voice.',
    'http://example.com/buddy-guy-profile.jpg',
    @buddy_guy_id
);

-- Albums for Buddy Guy
CALL CreateAlbumWithArtistsAndTracks(
    'Damn Right, I''ve Got the Blues',
    '1991-03-05',
    FALSE,
    'https://example.com/damn-right-ive-got-the-blues-cover.jpg',
    CONCAT('[{"artistId": ', @buddy_guy_id, ', "role": "main"}]'),
    '[{"title": "Damn Right, I''ve Got the Blues", "duration": 267000}, {"title": "Where Is the Next One Coming From?", "duration": 277000}, {"title": "Mustang Sally", "duration": 248000}, {"title": "Five Long Years", "duration": 269000}, {"title": "Black Night", "duration": 211000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Feels Like Rain',
    '1993-09-14',
    FALSE,
    'https://example.com/feels-like-rain-cover.jpg',
    CONCAT('[{"artistId": ', @buddy_guy_id, ', "role": "main"}]'),
    '[{"title": "Feels Like Rain", "duration": 294000}, {"title": "She''s a Superstar", "duration": 245000}, {"title": "Too Many Tears", "duration": 252000}, {"title": "I''ve Got Dreams to Remember", "duration": 280000}, {"title": "One Room Country Shack", "duration": 261000}]'
);

CALL CreateAlbumWithArtistsAndTracks(
    'Skin Deep',
    '2008-01-29',
    FALSE,
    'https://example.com/skin-deep-cover.jpg',
    CONCAT('[{"artistId": ', @buddy_guy_id, ', "role": "main"}]'),
    '[{"title": "Skin Deep", "duration": 255000}, {"title": "Every Time I Roll the Dice", "duration": 255000}, {"title": "Too Many Tears", "duration": 252000}, {"title": "Pride and Joy", "duration": 262000}, {"title": "Key to the Highway", "duration": 265000}]'
);


-- clean up
DROP PROCEDURE InsertArtistData;
DROP PROCEDURE CreateAlbumWithArtistsAndTracks;