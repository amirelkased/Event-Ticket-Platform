package org.elkased.eventticketplatform.config;

import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.filters.UserProvisioningFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   UserProvisioningFilter userProvisioningFilter,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter) {
        httpSecurity
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
                                .requestMatchers("/api/v1/events").hasRole("ORGANIZER")
                                .requestMatchers("/api/v1/ticket-validation").hasRole("STAFF")
                                .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
