package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.User;
import org.springframework.lang.NonNull;

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
}
