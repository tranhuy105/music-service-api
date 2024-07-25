-- Create subscription plans table
CREATE TABLE subscription_plans (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_months INT NOT NULL,
    features TEXT
);

-- Create subscriptions table
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    plan_id SMALLINT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_subscription_plans_name ON subscription_plans (name);
CREATE INDEX idx_subscriptions_user_id ON subscriptions (user_id);
CREATE INDEX idx_subscriptions_plan_id ON subscriptions (plan_id);
CREATE INDEX idx_subscriptions_end_date ON subscriptions (end_date);

-- Insert initial subscription plans
INSERT INTO subscription_plans (id, name, price, duration_months, features) VALUES
(1, 'Premium Monthly', 9.99, 1, 'Full features'),
(2, 'Premium Yearly', 99.99, 12, 'Full features and discount'),
(3, 'Premium Student', 4.99, 1, 'Full features, for students only');

-- Creating a stored procedure to handle the creation or update of a subscription
DELIMITER //

CREATE PROCEDURE CreateOrUpdateSubscription(
    IN p_user_id BIGINT,
    IN p_plan_id SMALLINT
)
BEGIN
    DECLARE v_start_date DATE;
    DECLARE v_end_date DATE;
    DECLARE v_duration INT;
    DECLARE v_current_end_date DATE;

    -- Get the duration of the new plan
    SELECT duration_months INTO v_duration
    FROM subscription_plans
    WHERE id = p_plan_id;

    -- Check if the user has an active subscription and get its end date
    SELECT MAX(end_date) INTO v_current_end_date
    FROM subscriptions
    WHERE user_id = p_user_id AND end_date >= CURDATE();

    IF v_current_end_date IS NOT NULL THEN
        -- Extend the current subscription
        SET v_end_date = DATE_ADD(v_current_end_date, INTERVAL v_duration MONTH);

        -- Update the current active subscription with the new end date
        UPDATE subscriptions
        SET plan_id = p_plan_id, end_date = v_end_date
        WHERE user_id = p_user_id AND end_date = v_current_end_date;
    ELSE
        -- No active subscription, start a new one
        SET v_start_date = CURDATE();
        SET v_end_date = DATE_ADD(v_start_date, INTERVAL v_duration MONTH);

        INSERT INTO subscriptions (user_id, plan_id, start_date, end_date)
        VALUES (p_user_id, p_plan_id, v_start_date, v_end_date);
    END IF;
END //

DELIMITER ;

-- Adding view to track user subscription
CREATE VIEW user_subscription_status AS
SELECT u.id AS user_id, u.email, s.plan_id, sp.name AS plan_name, s.start_date, s.end_date
FROM users u
LEFT JOIN subscriptions s ON u.id = s.user_id
LEFT JOIN subscription_plans sp ON s.plan_id = sp.id;


-- Admin should have premium plan :))
CALL CreateOrUpdateSubscription(1, 1);