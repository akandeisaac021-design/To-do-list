package com.todoapp.security;

import com.todoapp.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

/**
 * Replaces Spring Security's filter chain: any request outside the open
 * paths must carry a session with SESSION_USER_ID_KEY set (put there by
 * AuthController on successful login), or it gets a 401 here, before it
 * ever reaches a controller.
 */
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    public static final String SESSION_USER_ID_KEY = "userId";

    private static final Set<String> OPEN_PATHS =
            Set.of("/api/auth/signup", "/api/auth/login", "/api/auth/logout");

    private final ObjectMapper objectMapper;

    public SessionAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (OPEN_PATHS.contains(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID_KEY) == null) {
            respondUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void respondUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorResponse("Not authenticated"));
    }
}
