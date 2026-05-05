package com.localissue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/user/*/issues").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/user/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/issues/my-posts").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/issues").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/issues/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/issues").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/issues/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/issues/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/issues/*/vote").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/issues/*/comments").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/issues/*/status").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }
}
