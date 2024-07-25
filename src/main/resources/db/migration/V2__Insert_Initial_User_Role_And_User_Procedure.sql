DELIMITER //

CREATE PROCEDURE CreateUser(
    IN p_email VARCHAR(100),
    IN p_password VARCHAR(255)
)
BEGIN
    DECLARE v_user_id BIGINT;

    INSERT INTO users (email, password, account_locked, enabled)
    VALUES (p_email, p_password, FALSE, TRUE);

    SET v_user_id = LAST_INSERT_ID();

    INSERT INTO user_role (user_id, role_id)
    VALUES (v_user_id, 1);
END //

DELIMITER ;


-- Insert initial roles
INSERT INTO roles (id, name) VALUES
                             (1, 'ROLE_USER'),
                             (2, 'ROLE_ADMIN'),
                             (3, 'ROLE_MANAGER');

-- Insert the admin user
INSERT INTO users (email, password, account_locked, enabled)
VALUES ('admin@example.com', '$2a$10$/ixbgJIvnYpUCXR6sNw6FeMQuLfBuwdI1oHJWVk3hSGHMmczBcQai', FALSE, TRUE);

-- Get the user ID
SET @user_id = LAST_INSERT_ID();

-- Assign roles to the admin user
INSERT INTO user_role (user_id, role_id)
VALUES (@user_id, 1),
       (@user_id, 3);