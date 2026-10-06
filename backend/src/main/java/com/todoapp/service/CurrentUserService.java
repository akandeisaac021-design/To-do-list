package com.todoapp.service;

import com.todoapp.data.model.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the User attached to the current request's session. This is what
 * replaces Spring Security's @AuthenticationPrincipal.
 */
public interface CurrentUserService {

    /**
     * @throws com.todoapp.exception.UnauthenticatedException if there's no
     *         session or it has no logged-in user. In practice
     *         SessionAuthenticationFilter rejects such requests before they
     *         reach a controller, so this is a safety net, not the primary
     *         enforcement point.
     */
    User getCurrentUser(HttpServletRequest request);
}
