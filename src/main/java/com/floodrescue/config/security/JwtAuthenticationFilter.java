package com.floodrescue.config.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, java.io.IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String auth = request.getHeader("Authorization");

        log.debug("JwtAuthenticationFilter processing {} {} - Authorization header present: {}", method, requestUri, auth != null);

        if (auth == null || !auth.startsWith("Bearer ")) {
            log.debug("JwtAuthenticationFilter skipping authentication for {} {} - missing or invalid Authorization header", method, requestUri);
            chain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = auth.substring(7);

            try {
                Claims claims = jwtTokenProvider.parseToken(token).getBody();
                Long userId = Long.parseLong(claims.getSubject());

                UserDetails userDetails = userDetailsService.loadUserById(userId);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JwtAuthenticationFilter authenticated user {} for {} {}", userId, method, requestUri);
            } catch (Exception ex) {
                log.warn("JWT validation failed for {} {}: {}", method, requestUri, ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("JwtAuthenticationFilter found existing authentication for {} {}", method, requestUri);
        }

        chain.doFilter(request, response);
    }
}