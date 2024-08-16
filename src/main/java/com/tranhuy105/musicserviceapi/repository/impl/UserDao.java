package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.mapper.UserDetailsRowMapper;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.repository.api.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserDao implements UserRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<User> findByEmail(@NonNull String email) {
        String sql = "SELECT * FROM user_details WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, new UserDetailsRowMapper(), email);
        return users.stream().findFirst();
    }

    @Override
    public Optional<User> findById(@NonNull Long id) {
        String sql = "SELECT * FROM user_details WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserDetailsRowMapper(), id);
        return users.stream().findFirst();
    }

    @Override
    @Transactional
    public void insert(@NonNull User user) {
        String sql = "{CALL CreateUser(?, ?)}";
        jdbcTemplate.update(sql, user.getEmail(), user.getPassword());
    }

    @Override
    @Transactional
    public void update(@NonNull User user) {
        String sql = "UPDATE users SET firstname = ?, lastname = ?, dob = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getFirstname(), user.getLastname(), user.getDob(), user.getId());
    }

    @Override
    public void removeAllRolesFromUser(Long userId) {
        String sql = "DELETE FROM user_role WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void addRolesToUser(Long userId, List<Long> roleIds) {
        String sql = "INSERT INTO user_role (user_id, role_id) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (Long roleId : roleIds) {
            batchArgs.add(new Object[]{userId, roleId});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void deleteRolesFromUser(@NonNull Long userId, @NonNull List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM user_role WHERE user_id = ? AND role_id = ?";

        List<Object[]> batchArgs = new ArrayList<>();
        for (Long roleId : roleIds) {
            batchArgs.add(new Object[]{userId, roleId});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

}
