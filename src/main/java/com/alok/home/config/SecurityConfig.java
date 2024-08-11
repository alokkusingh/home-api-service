package com.alok.home.config;

import com.alok.home.commons.exception.InvalidTokenException;
import com.alok.home.commons.exception.UserNotAuthenticatedException;
import com.alok.home.model.CustomUserDetails;
import com.alok.home.model.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Set;


@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Autowired
   private TokenIssuerConfig tokenIssuerConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${application.id}")
    private String applicationId;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(customAuthFilter(), UsernamePasswordAuthenticationFilter.class)
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

    public OncePerRequestFilter customAuthFilter() {
        return new OncePerRequestFilter() {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                if ("localhost".equals(request.getHeader("host").split(":")[0])) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    "localhost",
                                    null,
                                    Set.of(new SimpleGrantedAuthority("ROLE_LOCALHOST"))
                            )
                    );

                    filterChain.doFilter(request, response);
                    return;
                }

                final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
                String issuer = request.getHeader("issuer");
                issuer = issuer ==  null? "google": issuer;
                String clientId = request.getHeader("client-id");

                if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                    filterChain.doFilter(request, response);
                    return;
                }

                if (!issuer.equals("google") && clientId == null) {
                    log.error("client-id is mandatory for non Google IdP token");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Get jwt token and validate
                Request authRequest = null;
                if (issuer.equals("google")) {
                    log.debug("Authenticating with google IdP");
                    authRequest = new Request.Builder()
                            .url(tokenIssuerConfig.getUrls().get(issuer))
                            .method("POST", body)
                            .addHeader("Authorization", bearerToken)
                            .build();
                } else {
                    log.debug("Authenticating with home-auth IdP");
                    authRequest = new Request.Builder()
                            .url(tokenIssuerConfig.getUrls().get(issuer))
                            .method("POST", body)
                            .addHeader("Authorization", bearerToken)
                            .addHeader("subject", clientId)
                            .addHeader("audience", applicationId)
                            .build();
                }


                UserInfo userInfo;
                try {
                    Response authResponse = client.newCall(authRequest).execute();
                    if (!authResponse.isSuccessful()) {
                        log.error("Auh Service return code: {}", authResponse.code());
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }

                    userInfo = objectMapper.readValue(authResponse.body().string(), UserInfo.class);
                    log.debug("Auth response: {}", userInfo);
                } catch (RuntimeException | ConnectException rte) {
                    rte.printStackTrace();
                    log.error("Error: Auth APi call failed");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return;
                }

                UserDetails userDetails = new CustomUserDetails(userInfo);
                UsernamePasswordAuthenticationToken
                        authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        userDetails.getAuthorities()
                );


                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
            }
        };
    }

}
