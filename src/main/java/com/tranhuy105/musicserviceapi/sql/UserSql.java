package com.tranhuy105.musicserviceapi.sql;

public final class UserSql {

    private UserSql() {
    }

    public static final String FIND_BY_EMAIL = """
        SELECT u.id, u.firstname, u.lastname, u.dob, u.email, u.password, u.account_locked, u.enabled, r.id AS role_id, r.name AS role_name,
        IF(EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = u.id AND s.end_date > CURDATE()), TRUE, FALSE) AS is_premium
        FROM users u
        LEFT JOIN user_role ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        WHERE u.email = ?
    """;

    public static final String FIND_BY_ID = """
        SELECT u.id, u.firstname, u.lastname, u.dob, u.email, u.password, u.account_locked, u.enabled, r.id AS role_id, r.name AS role_name,
        IF(EXISTS (SELECT 1 FROM subscriptions s WHERE s.user_id = u.id AND s.end_date > CURDATE()), TRUE, FALSE) AS is_premium
        FROM users u
        LEFT JOIN user_role ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        WHERE u.id = ?
    """;
}
