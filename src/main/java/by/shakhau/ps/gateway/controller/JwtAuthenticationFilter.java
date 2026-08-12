package by.shakhau.ps.gateway.controller;

import by.shakhau.ps.gateway.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String SESSION_ID_HEADER = "X-Session-Id";
    private static final String ROLES_HEADER = "X-Roles";

    private JwtService jwtService;

    @AllArgsConstructor
    @Getter
    public static class UserPrincipal implements UserDetails {

        private final UUID id;
        private final UUID sessionId;
        private final Collection<? extends GrantedAuthority> authorities;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getUsername() {
            return String.valueOf(id);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());

            var claims = jwtService.getClaims(token);
            if (claims == null || claims.getExpiration().before(new Date())) {
                return chain.filter(exchange);
            }

            UUID userId = UUID.fromString(claims.getSubject());
            UUID sessionId = UUID.fromString((String) claims.get("session_id"));

            List<String> roles = claims.get("roles", List.class);
            if (roles.isEmpty()) {
                return chain.filter(exchange);
            }

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            var principal = new UserPrincipal(userId, sessionId, authorities);

            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            var securityContext = new SecurityContextImpl(authentication);

            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.remove(SESSION_ID_HEADER);
                        headers.remove(ROLES_HEADER);

                        headers.set(USER_ID_HEADER, userId.toString());
                        headers.set(SESSION_ID_HEADER, sessionId.toString());
                        headers.set(ROLES_HEADER, String.join(",", roles));
                    })
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();
            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder
                            .withSecurityContext(Mono.just(securityContext)));
        }

        return chain.filter(exchange);
    }
}
