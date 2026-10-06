package com.todoapp.service;

import com.todoapp.dto.request.LoginRequest;
import com.todoapp.dto.request.SignupRequest;
import com.todoapp.data.model.User;

public interface AuthService {

    User signup(SignupRequest request);

    User login(LoginRequest request);
}
