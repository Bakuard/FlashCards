package com.bakuard.flashcards.dal.fragment;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.validation.InvalidParameter;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class UserSaverImpl implements UserSaver<User> {

    private JdbcTemplate jdbcTemplate;
    private JdbcAggregateOperations jdbcAggregateOperations;
    private ConfigData configData;

    public UserSaverImpl(JdbcTemplate jdbcTemplate,
                         JdbcAggregateOperations jdbcAggregateOperations,
                         ConfigData configData) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcAggregateOperations = jdbcAggregateOperations;
        this.configData = configData;
    }

    @Override
    public User save(User user) {
        if(user.hasRole(configData.superAdmin().roleName())) {
            UUID superAdminId = jdbcTemplate.query("""
                select users.user_id
                    from users
                    inner join roles
                        on users.user_id = roles.user_id
                           and roles.name = ?;
                """,
                    ps -> ps.setString(1, configData.superAdmin().roleName()),
                    rs -> {
                        UUID result = null;
                        if(rs.next()) {
                            result = (UUID) rs.getObject("user_id");
                        }
                        return result;
                    }
            );

            if(superAdminId != null && !superAdminId.equals(user.getId())) {
                throw new InvalidParameter("User.superAdmin.unique");
            }
        }

        return jdbcAggregateOperations.save(user);
    }

}
