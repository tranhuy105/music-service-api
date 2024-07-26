package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Role;
import com.tranhuy105.musicserviceapi.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class UserDetailsRowMapper implements RowMapper<User> {
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
