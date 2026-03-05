package com.floodrescue.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, java.io.IOException {
    
        String auth = request.getHeader("Authorization");
    
        if (auth != null && auth.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = auth.substring(7).trim();
            
            // Validate token format: JWT must have exactly 2 periods (header.payload.signature)
            if (token.isEmpty() || token.split("\\.").length != 3) {
                // Invalid token format, skip authentication
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }
    
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
            } catch (JwtException | IllegalArgumentException ex) {
                // Invalid or expired token - clear context and continue
                // Don't log stack trace for invalid tokens (common case)
                SecurityContextHolder.clearContext();
            } catch (Exception ex) {
                // Other unexpected errors - log but don't break the request
                System.err.println("[JwtAuthenticationFilter] Unexpected error: " + ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
    
        chain.doFilter(request, response);
    }}