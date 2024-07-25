package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.model.SubscriptionPlan;
import com.tranhuy105.musicserviceapi.model.UserSubscription;
import com.tranhuy105.musicserviceapi.repository.api.SubscriptionRepository;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SubscriptionDao implements SubscriptionRepository {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDao.class);
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_USER_SUBSCRIPTIONS_SQL =
            "SELECT * FROM user_subscription_status WHERE user_id = ?";

    private static final String FIND_ALL_SUBSCRIPTION_PLANS_SQL =
            "SELECT * FROM subscription_plans";

    @Override
    public List<UserSubscription> findUserSubscriptions(@NonNull Long userId) {
        return jdbcTemplate.query(FIND_USER_SUBSCRIPTIONS_SQL, new UserSubscriptionRowMapper(), userId);
    }

    @Override
    public List<SubscriptionPlan> findAllSubscriptionPlan() {

        return jdbcTemplate.query(FIND_ALL_SUBSCRIPTION_PLANS_SQL, new SubscriptionPlanRowMapper());
    }

    @Override
    @Transactional
    public void createSubscriptionPlan(@NonNull Long userId, @NonNull Short planId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("CreateOrUpdateSubscription");
        try {
            jdbcCall.execute(Map.of(
                    "p_user_id", userId,
                    "p_plan_id", planId
            ));
        } catch (DataAccessException e) {
            logger.error("Error executing stored procedure: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create subscription plan: " + e.getMessage(), e);
        }
    }

    private static final class UserSubscriptionRowMapper implements RowMapper<UserSubscription> {
        @Override
        public UserSubscription mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserSubscription subscription = new UserSubscription();
            subscription.setUserId(rs.getLong("user_id"));
            subscription.setEmail(rs.getString("email"));
            subscription.setPlanId(rs.getShort("plan_id"));
            subscription.setPlanName(rs.getString("plan_name"));
            subscription.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            subscription.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            return subscription;
        }
    }

    private static final class SubscriptionPlanRowMapper implements RowMapper<SubscriptionPlan> {
        @Override
        public SubscriptionPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
            SubscriptionPlan plan = new SubscriptionPlan();
            plan.setId(rs.getShort("id"));
            plan.setName(rs.getString("name"));
            plan.setPrice(rs.getBigDecimal("price"));
            plan.setDurationMonths(rs.getInt("duration_months"));
            plan.setFeatures(rs.getString("features"));
            return plan;
        }
    }
}
