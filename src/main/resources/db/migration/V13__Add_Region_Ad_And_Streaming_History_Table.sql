-- Regions table
CREATE TABLE regions (
    code CHAR(2) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Advertisements table
CREATE TABLE advertisements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    target_url VARCHAR(255),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    region_code CHAR(2) UNIQUE NOT NULL,
    FOREIGN KEY (region_code) REFERENCES regions(code)
);

CREATE INDEX idx_end_date_id ON advertisements (end_date, id);
CREATE INDEX idx_end_date_region ON advertisements (end_date, region_code);
CREATE INDEX idx_region ON advertisements (region_code);

-- history table
CREATE TABLE streaming_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    track_id BIGINT NOT NULL,
    listening_time BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
);

INSERT INTO regions (code, name) VALUES
('US', 'United States'),
('CA', 'Canada'),
('MX', 'Mexico'),
('GB', 'United Kingdom'),
('FR', 'France'),
('DE', 'Germany'),
('JP', 'Japan'),
('CN', 'China'),
('KR', 'South Korea'),
('BR', 'Brazil'),
('AR', 'Argentina'),
('AU', 'Australia'),
('IN', 'India'),
('ZA', 'South Africa'),
('IT', 'Italy'),
('ES', 'Spain'),
('RU', 'Russia'),
('ID', 'Indonesia'),
('NG', 'Nigeria'),
('PK', 'Pakistan'),
('PH', 'Philippines'),
('VN', 'Vietnam'),
('TH', 'Thailand'),
('MY', 'Malaysia'),
('SG', 'Singapore'),
('SA', 'Saudi Arabia'),
('TR', 'Turkey'),
('EG', 'Egypt'),
('NZ', 'New Zealand'),
('PE', 'Peru'),
('CL', 'Chile'),
('CO', 'Colombia'),
('UA', 'Ukraine'),
('PL', 'Poland'),
('BE', 'Belgium'),
('CH', 'Switzerland'),
('SE', 'Sweden'),
('NO', 'Norway'),
('DK', 'Denmark'),
('FI', 'Finland'),
('IE', 'Ireland');

INSERT INTO advertisements (title, description, image_url, target_url, start_date, end_date, region_code) VALUES
('Summer Music Festival 2024', 'Join us for an unforgettable summer music festival with top artists from around the world. Tickets are selling fast!', 'https://example.com/summer-music-festival.jpg', 'https://example.com/summer-music-festival', NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 'US'),
('Sơn Tùng M-TP: New Album Release', 'Discover the latest album from Sơn Tùng M-TP. Stream now and enjoy exclusive tracks from Vietnam’s top artist!', 'https://example.com/son-tung-mtp-new-album.jpg', 'https://example.com/son-tung-mtp-new-album', NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH), 'VN'),
('50% Off on All Music Merch', 'Get 50% off on all music merchandise. Limited time offer!', 'https://example.com/music-merch.jpg', 'https://example.com/music-merch', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 'CA'),
('Exclusive Concert Streaming', 'Watch exclusive live concert streams from top artists. Available only in your region!', 'https://example.com/concert-streaming.jpg', 'https://example.com/concert-streaming', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 'JP'),
('Holiday Playlist Specials', 'Create the perfect holiday playlist with our specially curated tracks. Available now!', 'https://example.com/holiday-playlist.jpg', 'https://example.com/holiday-playlist', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 'AU');
