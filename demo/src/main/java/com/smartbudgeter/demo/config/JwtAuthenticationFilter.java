package com.smartbudgeter.demo.config;

import com.smartbudgeter.demo.services.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import io.jsonwebtoken.JwtException; 


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // Use logger

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService; 
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Inside JwtAuthenticationFilter.doFilterInternal, right at the beginning
logger.info("+++++ JwtAuthenticationFilter invoked for URI: {} {}", request.getMethod(), request.getRequestURI());

        logger.debug(">>> JwtAuthenticationFilter processing request: {} {}", request.getMethod(), request.getRequestURI());

        String header = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (header != null && header.startsWith("Bearer ")) {
            jwt = header.substring(7);
            logger.debug("JWT Token extracted (length: {})", jwt != null ? jwt.length() : 0);
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                logger.debug("Username extracted from token: {}", username);
            } catch (JwtException e) { 
                logger.warn("JWT Token extraction failed (invalid token structure/claims)", e);
                chain.doFilter(request, response); 
                return; 
            } catch (Exception e) {
                logger.error("Unexpected error extracting username from JWT Token", e);
                chain.doFilter(request, response);
                return;
            }
        } else {
            logger.debug("No Bearer token found in Authorization header.");
        }

        // Check if not already authenticated AND we have a username
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Attempting to load user details for username: {}", username);
            UserDetails userDetails = null;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.debug("User details loaded for username: {}", userDetails.getUsername());
            } catch (Exception e) {
                logger.error("Failed to load user details for username: {}", username, e);
                chain.doFilter(request, response);
                return;
            }

            if (userDetails != null) {
                logger.debug("Attempting to validate token for user: {}", userDetails.getUsername());
                boolean tokenValid = false;
                try {
                    tokenValid = jwtUtil.validateToken(jwt, userDetails.getUsername());
                    logger.debug("Token validation result for user {}: {}", userDetails.getUsername(), tokenValid);
                } catch (Exception e) {
                    logger.error("Exception during token validation for user: {}", userDetails.getUsername(), e);
                    tokenValid = false;
                }

                if (tokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    logger.debug("Setting Authentication in SecurityContext: {}", authToken);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("<<< Authentication SUCCESSFULLY set in SecurityContext for user: {}", username);
                } else {
                    logger.warn("<<< Token validation FAILED for user: {}", username);
                }
            } else {
                logger.warn("<<< User details were null, skipping authentication for username: {}", username);
            }
        } else {
            if (username == null) {
                logger.debug("<<< Username was null, skipping authentication logic.");
            } else {
                Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
                logger.debug("<<< SecurityContext already contains Authentication (authenticated: {}). Skipping filter logic.", existingAuth != null ? existingAuth.isAuthenticated() : "null");
            }
        }
        logger.debug("<<< JwtAuthenticationFilter continuing with filter chain.");
        // Inside JwtAuthenticationFilter.doFilterInternal, right before the final chain.doFilter
logger.info("+++++ JwtAuthenticationFilter about to call chain.doFilter for URI: {} {}", request.getMethod(), request.getRequestURI());
Authentication authCheck = SecurityContextHolder.getContext().getAuthentication();
logger.info("+++++ SecurityContext Authentication before chain.doFilter: {}", authCheck);
// chain.doFilter(request, response);
        chain.doFilter(request, response);
    }
}