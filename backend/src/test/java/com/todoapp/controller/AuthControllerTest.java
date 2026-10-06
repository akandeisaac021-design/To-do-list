package com.todoapp.controller;

import com.todoapp.dto.request.LoginRequest;
import com.todoapp.dto.request.SignupRequest;
import com.todoapp.data.model.User;
import com.todoapp.exception.InvalidCredentialsException;
import com.todoapp.exception.UsernameAlreadyInUseException;
import com.todoapp.security.SessionAuthenticationFilter;
import com.todoapp.service.AuthService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SessionAuthenticationFilter (a @Component Filter bean) is picked up
 * automatically by @WebMvcTest, same as it would be in the full app - and
 * since /api/auth/signup and /api/auth/login are its open paths, that's
 * transparent here, so no addFilters = false is needed.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    class Signup {

        @Test
        void returns201AndTheCreatedUserOnSuccess() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername("newuser");
            request.setPassword("plainPassword123");

            when(authService.signup(any())).thenReturn(
                    User.builder().id(1L).username("newuser").build());

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("newuser"));
        }

        @Test
        void returns409WhenUsernameIsAlreadyRegistered() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername("taken");
            request.setPassword("plainPassword123");

            when(authService.signup(any())).thenThrow(new UsernameAlreadyInUseException("taken"));

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        void returns400ForAUsernameShorterThanThreeCharacters() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername("ab");
            request.setPassword("plainPassword123");

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns400ForAPasswordUnderEightCharacters() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername("newuser");
            request.setPassword("short");

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Login {

        @Test
        void returns200AndTheUserOnSuccess() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("someuser");
            request.setPassword("plainPassword123");

            when(authService.login(any())).thenReturn(
                    User.builder().id(1L).username("someuser").build());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("someuser"));
        }

        @Test
        void putsTheUserIdInTheSessionOnSuccess() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("someuser");
            request.setPassword("plainPassword123");

            when(authService.login(any())).thenReturn(
                    User.builder().id(1L).username("someuser").build());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(request().sessionAttribute(
                            SessionAuthenticationFilter.SESSION_USER_ID_KEY, notNullValue()));
        }

        @Test
        void returns401ForWrongCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("someuser");
            request.setPassword("wrongPassword");

            when(authService.login(any())).thenThrow(new InvalidCredentialsException());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void returns400WhenPasswordIsBlank() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("someuser");
            request.setPassword("");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Logout {

        @Test
        void returns204AndInvalidatesAnExistingSession() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionAuthenticationFilter.SESSION_USER_ID_KEY, 1L);

            mockMvc.perform(post("/api/auth/logout").session(session))
                    .andExpect(status().isNoContent());

            org.junit.jupiter.api.Assertions.assertTrue(session.isInvalid());
        }

        @Test
        void returns204EvenWithNoExistingSession() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isNoContent());
        }
    }
}
