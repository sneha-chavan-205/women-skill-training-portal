package com.wstp.portal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // CSRF disabled for the current API-based login/register setup
            .csrf(csrf -> csrf.disable())

            // Redirect unauthenticated browser users to our custom login page
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(
                    new LoginUrlAuthenticationEntryPoint("/login")
                )
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // Public pages and resources
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/api/users/register",
                    "/api/auth/login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // Dashboard requires authentication
                .requestMatchers("/dashboard").authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}