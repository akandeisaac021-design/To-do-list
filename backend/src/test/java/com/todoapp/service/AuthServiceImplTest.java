package com.todoapp.service;

import com.todoapp.dto.request.LoginRequest;
import com.todoapp.dto.request.SignupRequest;
import com.todoapp.data.model.User;
import com.todoapp.exception.InvalidCredentialsException;
import com.todoapp.exception.UsernameAlreadyInUseException;
import com.todoapp.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.todoapp.security.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder);
    }

    @Nested
    class Signup {

        @Test
        void hashesThePasswordAndSavesANewUser() {
            SignupRequest request = new SignupRequest();
            request.setUsername("newuser");
            request.setPassword("plainPassword123");

            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plainPassword123")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            authService.signup(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getUsername()).isEqualTo("newuser");
            assertThat(saved.getPassword()).isEqualTo("hashedPassword");
        }

        @Test
        void returnsThePersistedUserOnSuccess() {
            SignupRequest request = new SignupRequest();
            request.setUsername("newuser");
            request.setPassword("plainPassword123");

            User persisted = User.builder().id(1L).username("newuser").password("hashedPassword").build();
            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(persisted);

            User result = authService.signup(request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("newuser");
        }

        @Test
        void throwsWhenUsernameIsAlreadyRegistered() {
            SignupRequest request = new SignupRequest();
            request.setUsername("taken");
            request.setPassword("plainPassword123");

            when(userRepository.findByUsername("taken"))
                    .thenReturn(Optional.of(User.builder().username("taken").build()));

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(UsernameAlreadyInUseException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class Login {

        @Test
        void returnsTheUserWhenCredentialsAreCorrect() {
            LoginRequest request = new LoginRequest();
            request.setUsername("someuser");
            request.setPassword("plainPassword123");

            User existing = User.builder().id(1L).username("someuser").password("hashedPassword").build();
            when(userRepository.findByUsername("someuser")).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches("plainPassword123", "hashedPassword")).thenReturn(true);

            User result = authService.login(request);

            assertThat(result).isEqualTo(existing);
        }

        @Test
        void throwsInvalidCredentialsWhenUsernameDoesNotExist() {
            LoginRequest request = new LoginRequest();
            request.setUsername("nobody");
            request.setPassword("whatever123");

            when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void throwsInvalidCredentialsWhenPasswordDoesNotMatch() {
            LoginRequest request = new LoginRequest();
            request.setUsername("someuser");
            request.setPassword("wrongPassword");

            User existing = User.builder().username("someuser").password("hashedPassword").build();
            when(userRepository.findByUsername("someuser")).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
