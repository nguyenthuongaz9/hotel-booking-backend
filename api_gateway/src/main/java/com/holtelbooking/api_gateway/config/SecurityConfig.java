package com.holtelbooking.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveJwtDecoder reactiveJwtDecoder;

    public SecurityConfig(ReactiveJwtDecoder reactiveJwtDecoder) {
        this.reactiveJwtDecoder = reactiveJwtDecoder;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/eureka/**", 
                                "/actuator/**", 
                                "/api/auth/**", 
                                "/api/public/**", 
                                "/api/rooms/**",
                                "/api/orders/**",
                                "/uploads/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()
                        // Allow OPTIONS requests for CORS preflight
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder))
                );

        return http.build();
    }
}