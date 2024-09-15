package com.alok.home.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OncePerRequestFilter customAuthenticationFilter) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator", "/actuator/**").permitAll();
                    auth.requestMatchers("/odion/**").hasAnyRole("ADMIN", "USER", "home_api_ro", "home_api_rw", "LOCALHOST");
                    auth.requestMatchers("/expense", "/expense/**").hasAnyRole("ADMIN", "USER", "home_api_ro", "home_api_rw", "LOCALHOST");
                    auth.requestMatchers("/investment", "/investment/**").hasAnyRole("ADMIN", "USER", "home_api_ro", "home_api_rw", "LOCALHOST");
                    auth.requestMatchers("/tax", "/tax/**").hasAnyRole("ADMIN", "USER", "home_api_ro", "home_api_rw", "LOCALHOST");

                    auth.requestMatchers("/summary/**").hasAnyRole("ADMIN", "home_api_ro", "home_api_rw", "LOCALHOST");
                    auth.requestMatchers("/bank", "/bank/**").hasAnyRole("ADMIN", "home_api_ro", "home_api_rw", "LOCALHOST");
                    auth.requestMatchers("/cache", "/cache/**").hasAnyRole("ADMIN", "home_api_ro", "home_api_rw", "LOCALHOST");

                    auth.anyRequest().authenticated();
                })
                //.httpBasic(Customizer.withDefaults())
                .build();
    }
}
