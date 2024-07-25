package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.model.Role;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.repository.api.UserRepository;
import com.tranhuy105.musicserviceapi.sql.UserSql;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserDao implements UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    @Override
    public Optional<User> findByEmail(@NonNull String email) {
        List<User> users = jdbcTemplate.query(UserSql.FIND_BY_EMAIL, new UserWithRolesRowMapper(), email);
        return users.stream().findFirst();
    }

    @Override
    public Optional<User> findById(@NonNull Long id) {
        List<User> users = jdbcTemplate.query(UserSql.FIND_BY_ID, new UserWithRolesRowMapper(), id);
        return users.stream().findFirst();
    }

    @Override
    @Transactional
    public void insert(@NonNull User user) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("CreateUser");

        try {
            jdbcCall.execute(Map.of(
                    "p_email", user.getEmail(),
                    "p_password", user.getPassword()
            ));
        } catch (DataAccessException e) {
            logger.error("Error executing stored procedure: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }


    private static final class UserWithRolesRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long userId = rs.getLong("id");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            Date dobDate = rs.getDate("dob");
            LocalDate dob = dobDate == null ? null : dobDate.toLocalDate();
            String email = rs.getString("email");
            String password = rs.getString("password");
            boolean accountLocked = rs.getBoolean("account_locked");
            boolean enabled = rs.getBoolean("enabled");
            boolean isPremium = rs.getBoolean("is_premium");

            User user = new User();
            user.setId(userId);
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setDob(dob);
            user.setEmail(email);
            user.setPassword(password);
            user.setAccountLocked(accountLocked);
            user.setEnabled(enabled);
            user.setRoles(new ArrayList<>());
            user.setIsPremium(isPremium);

            do {
                short roleId = rs.getShort("role_id");
                String roleName = rs.getString("role_name");
                if (roleId != 0 && roleName != null) {
                    Role role = new Role(roleId, roleName);
                    user.getRoles().add(role);
                }
            } while (rs.next());

            return user;
        }
    }
}
