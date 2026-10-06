package com.todoapp.security;

/**
 * Hashes and verifies passwords. This is our own abstraction now that
 * Spring Security's PasswordEncoder isn't available - AuthServiceImpl
 * depends on this interface, not on a concrete hashing algorithm.
 */
public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
