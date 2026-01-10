package com.amaravathi.tradeidentity.security;

import com.amaravathi.tradeidentity.domain.user.AppUserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AppUserRepository userRepo;

    public JwtAuthFilter(JwtTokenService jwtTokenService, AppUserRepository userRepo) {
        this.jwtTokenService = jwtTokenService;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String jwt = header.substring(7);
            Claims claims = jwtTokenService.parseAndValidate(jwt).getPayload();
            String userId = claims.getSubject();

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Optional check: user exists & active
                var user = userRepo.findById(Integer.valueOf(userId))
                        .orElseThrow();

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles", List.class);

                var authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

                var principal = new SecurityUser(user.getId(), user.getEmail(), authorities);
                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(request, response);
        } catch (Exception ex) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid or expired access token\"}");
        }
    }
}
