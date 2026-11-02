package com.todoapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Unrelated to Spring Security (which this project doesn't use) - CORS is
 * plain Spring MVC. Needed because the frontend runs on a different origin
 * (Vite's dev server on :5173) than the backend (:8080), and browsers block
 * cross-origin requests - including ones that would send/receive cookies -
 * unless the server explicitly allows it.
 *
 * allowCredentials(true) is what lets the session cookie travel cross-origin
 * at all. Browsers refuse to combine that with a wildcard origin, which is
 * why this reads a specific allowed origin from config instead of using "*".
 */
@Configuration
public class CorsConfig {

    @Value("${app.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigin)
                        .allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
