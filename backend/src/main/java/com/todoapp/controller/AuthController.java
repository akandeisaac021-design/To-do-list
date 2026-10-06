package com.todoapp.controller;

import com.todoapp.dto.request.LoginRequest;
import com.todoapp.dto.request.SignupRequest;
import com.todoapp.dto.response.AuthResponse;
import com.todoapp.data.model.User;
import com.todoapp.security.SessionAuthenticationFilter;
import com.todoapp.service.AuthService;
import com.todoapp.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody @Valid SignupRequest request) {
        User user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        User user = authService.login(request);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SessionAuthenticationFilter.SESSION_USER_ID_KEY, user.getId());

        return ResponseEntity.ok(toAuthResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Lets the frontend ask "is there already a valid session?" on page
     * load, e.g. after a browser refresh. Not open in SessionAuthenticationFilter
     * on purpose - a 401 here IS the "not logged in" signal the frontend needs.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(HttpServletRequest httpRequest) {
        User user = currentUserService.getCurrentUser(httpRequest);
        return ResponseEntity.ok(toAuthResponse(user));
    }

    private AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder().id(user.getId()).username(user.getUsername()).build();
    }
}
