package com.todoapp.service;

import com.todoapp.dto.request.LoginRequest;
import com.todoapp.dto.request.SignupRequest;
import com.todoapp.entity.User;

public interface AuthService {

    /**
     * Creates a new user with the given (unique) username and a hashed
     * password. Throws UsernameAlreadyInUseException if the username is taken.
     */
    User signup(SignupRequest request);

    /**
     * Verifies the given credentials against what's stored in the database.
     * Throws InvalidCredentialsException if the username doesn't exist or
     * the password doesn't match. Does not establish a session itself -
     * that's the controller's responsibility (see AuthController).
     */
    User login(LoginRequest request);
}
