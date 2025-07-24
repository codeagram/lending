package com.lending.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuration class for JPA auditing.
 * Enables JPA auditing and provides AuditorAware bean for tracking the current user.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    /**
     * Provides the current auditor (current user) for JPA auditing.
     * Uses the currently authenticated user's username or 'system' if not authenticated.
     *
     * @return AuditorAware implementation
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                        return ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
                    } else if (authentication.getPrincipal() instanceof String) {
                        return (String) authentication.getPrincipal();
                    }
                    return "system";
                });
    }
}
