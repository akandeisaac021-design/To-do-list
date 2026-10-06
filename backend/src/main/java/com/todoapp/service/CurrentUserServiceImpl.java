package com.todoapp.service;

import com.todoapp.data.model.User;
import com.todoapp.exception.UnauthenticatedException;
import com.todoapp.data.repository.UserRepository;
import com.todoapp.security.SessionAuthenticationFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthenticatedException();
        }

        Long userId = (Long) session.getAttribute(SessionAuthenticationFilter.SESSION_USER_ID_KEY);
        if (userId == null) {
            throw new UnauthenticatedException();
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }
}
