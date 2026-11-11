package edu.dosw.styleradar.chat.infrastructure.adapter.out.security;

import edu.dosw.styleradar.chat.infrastructure.exception.SecurityProblemHandler;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Authenticates each request from its Bearer JWT. */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenAdapter tokens;
    private final SecurityProblemHandler errors;

    public JwtAuthenticationFilter(JwtTokenAdapter tokens, SecurityProblemHandler errors) {
        this.tokens = tokens;
        this.errors = errors;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            AuthenticatedUser user = tokens.validate(header.substring(7).trim());
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    user, null, user.authorities()));
            SecurityContextHolder.setContext(context);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            log.warn("Token Bearer rechazado");
            errors.commence(request, response, new BadCredentialsException("Token inválido"));
            return;
        }
        chain.doFilter(request, response);
    }
}
