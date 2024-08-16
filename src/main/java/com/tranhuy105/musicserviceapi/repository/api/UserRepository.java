package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.User;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user to find, must not be null
     * @return an {@link Optional} containing the {@link User} if found, or an empty {@link Optional} if not found
     */
    Optional<User> findByEmail(@NonNull String email);

    /**
     * Finds a user by their unique identifier (ID).
     *
     * @param id the unique identifier of the user to find, must not be null
     * @return an {@link Optional} containing the {@link User} if found, or an empty {@link Optional} if not found
     */
    Optional<User> findById(@NonNull Long id);

    /**
     * Inserts a new user into the data store.
     *
     * <p>
     * This method is responsible for persisting a new {@link User} entity in the data store.
     * The user should have a unique email address. The implementation should handle the case
     * where the email already exists and throw an appropriate exception if necessary.
     * </p>
     *
     * @param user the {@link User} to insert, must not be null
     */
    void insert(@NonNull User user);
    void update(@NonNull User user);

    /**
     * Removes all roles from a user.
     *
     * <p>
     * This method deletes all entries in the {@code user_role} table for the specified user ID.
     * It effectively removes all roles assigned to the user.
     * </p>
     *
     * @param userId the unique identifier of the user whose roles are to be removed, must not be null
     */
    void removeAllRolesFromUser(Long userId);

    /**
     * Adds multiple roles to a user.
     *
     * <p>
     * This method inserts multiple role assignments for a user into the {@code user_role} table.
     * If some or all roles already exist for the user, duplicates will be avoided.
     * </p>
     *
     * @param userId the unique identifier of the user to whom roles are to be assigned, must not be null
     * @param roleIds a list of role IDs to assign to the user, must not be null or empty
     */
    void addRolesToUser(Long userId, List<Long> roleIds);

    /**
     * Deletes multiple roles from a user
     *
     * <p>
     * This method deletes multiple role assignments from the {@code user_role} table for the specified user ID.
     * If some of the roles are not assigned to the user, they will be ignored.
     * </p>
     *
     * @param userId the unique identifier of the user from whom the roles are to be removed, must not be null
     * @param roleIds a list of role IDs to remove from the user, must not be null or empty
     */
    void deleteRolesFromUser(@NonNull Long userId, @NonNull List<Long> roleIds);
}
