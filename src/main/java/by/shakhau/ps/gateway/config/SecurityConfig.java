package by.shakhau.ps.gateway.config;

import by.shakhau.ps.gateway.controller.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(headers -> headers.cache(ServerHttpSecurity.HeaderSpec.CacheSpec::disable))
                .authorizeExchange(exchanges -> exchanges
                        // Auth service
                        .pathMatchers(GET, "/auth/public-key").permitAll()
                        .pathMatchers(POST, "/auth/login").permitAll()
                        .pathMatchers(POST, "/auth/token/refresh").permitAll()
                        .pathMatchers(POST, "/auth/users").permitAll()
                        .pathMatchers(POST, "/auth/users/create-admin").permitAll()
                        .pathMatchers(PATCH, "/auth/users/change-password").permitAll()

                        .pathMatchers(GET, "/auth/users/me").authenticated()
                        .pathMatchers(GET, "/auth/users/me/roles").authenticated()

                        .pathMatchers(GET, "/auth/roles").hasRole("ADMIN")
                        .pathMatchers(GET, "/auth/users/*/roles").hasRole("ADMIN")
                        .pathMatchers(GET, "/auth/users/*/roles/**").hasRole("ADMIN")
                        .pathMatchers("/auth/users/**", "/auth/users").hasRole("ADMIN")

                        // User service
                        .pathMatchers(HttpMethod.GET, "/users/me").authenticated()
                        .pathMatchers(HttpMethod.GET, "/payment-cards/users/me").authenticated()
                        .pathMatchers("/users/**", "/payment-cards/**").hasRole("ADMIN")

                        // Product service
                        .pathMatchers(HttpMethod.GET, "/products").permitAll()
                        .pathMatchers(HttpMethod.GET, "/products/*").permitAll()
                        .pathMatchers(HttpMethod.POST, "/products/filter").permitAll()

                        .pathMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/products/list").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")

                        // Order service
                        .pathMatchers(HttpMethod.GET, "/orders/me").authenticated()
                        .pathMatchers(HttpMethod.GET, "/orders/*/me").authenticated()
                        .pathMatchers(HttpMethod.POST, "/orders").authenticated()

                        .pathMatchers(HttpMethod.GET, "/orders/filtered").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/orders/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/orders/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/orders/**").hasRole("ADMIN")

                        // Payment service
                        .pathMatchers(HttpMethod.GET, "/payments/me").authenticated()
                        .pathMatchers(HttpMethod.GET, "/payments/total-sum/me").authenticated()
                        .pathMatchers(HttpMethod.GET, "/payments").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/payments/total-sum").hasRole("ADMIN")

                        .anyExchange().authenticated())
                .addFilterAfter(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
