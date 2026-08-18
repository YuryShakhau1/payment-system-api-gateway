package by.shakhau.ps.gateway.config;

import by.shakhau.ps.gateway.controller.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.cache(ServerHttpSecurity.HeaderSpec.CacheSpec::disable))
                .authorizeExchange(exchanges -> exchanges

                        // Actuator
                        .pathMatchers("/actuator/**").permitAll()

                        // Auth service
                        .pathMatchers(POST, "/auth/login").permitAll()
                        .pathMatchers(GET, "/auth/public-key").permitAll()
                        .pathMatchers(POST, "/auth/token/refresh").permitAll()
                        .pathMatchers(PATCH, "/auth/users/change-password").permitAll()

                        .pathMatchers(POST, "/auth/logout").authenticated()
                        .pathMatchers(POST, "/auth/logout/all").authenticated()
                        .pathMatchers(GET, "/auth/roles").authenticated()
                        .pathMatchers(GET, "/auth/users/roles/me").authenticated()
                        .pathMatchers(GET, "/auth/users/me").authenticated()

                        .pathMatchers("/auth/**").hasRole("ADMIN")

                        // User service
                        .pathMatchers(POST, "/users/create-admin").permitAll()

                        .pathMatchers(GET, "/users/payment-cards/me").authenticated()
                        .pathMatchers(GET, "/users/me").authenticated()
                        .pathMatchers(POST, "/users/payment-cards/me").authenticated()
                        .pathMatchers(PUT, "/users/me").authenticated()
                        .pathMatchers(PUT, "/users/payment-cards/*/me").authenticated()
                        .pathMatchers(PATCH, "/users/payment-cards/*/me").authenticated()
                        .pathMatchers(DELETE, "/users/payment-cards/*/me").authenticated()

                        .pathMatchers("/users/**").hasRole("ADMIN")

                        // Product service
                        .pathMatchers(GET, "/products").permitAll()
                        .pathMatchers(GET, "/products/*").permitAll()
                        .pathMatchers(POST, "/products/filter").permitAll()

                        .pathMatchers("/products/**").hasRole("ADMIN")

                        // Order service
                        .pathMatchers(GET, "/orders/me").authenticated()
                        .pathMatchers(GET, "/orders/me/filtered").authenticated()
                        .pathMatchers(POST, "/orders").authenticated()
                        .pathMatchers(POST, "/orders/*/pay/**").authenticated()
                        .pathMatchers("/orders/*/me").authenticated()

                        .pathMatchers("/orders/**").hasRole("ADMIN")

                        // Payment service
                        .pathMatchers(GET, "/payments/me").authenticated()
                        .pathMatchers(GET, "/payments/*/me").authenticated()
                        .pathMatchers(GET, "/payments/total-sum/me").authenticated()

                        .pathMatchers("/payments/**").hasRole("ADMIN")

                        .anyExchange().authenticated())
                .addFilterAfter(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*"); 

        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
