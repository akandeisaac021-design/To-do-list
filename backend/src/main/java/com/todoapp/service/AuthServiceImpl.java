package com.todoapp.service;

import com.todoapp.dto.request.LoginRequest;
import com.todoapp.dto.request.SignupRequest;
import com.todoapp.data.model.User;
import com.todoapp.exception.InvalidCredentialsException;
import com.todoapp.exception.UsernameAlreadyInUseException;
import com.todoapp.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.todoapp.security.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User signup(SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyInUseException(request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    public User login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
