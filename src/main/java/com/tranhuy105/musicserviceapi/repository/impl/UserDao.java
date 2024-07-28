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
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

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



}
