CREATE TABLE artist_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    artist_name VARCHAR(255) NOT NULL,
    genre VARCHAR(255) NULL,
    portfolio_url VARCHAR(255) NULL,
    bio TEXT NULL,
    social_media_links VARCHAR(500) NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_date TIMESTAMP NULL,
    reviewed_by BIGINT NULL,
    reason TEXT NULL,
    CONSTRAINT fk_artist_request_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_artist_request_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE INDEX idx_user_id ON artist_request(user_id);
