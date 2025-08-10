package com.smartbudgeter.demo.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Import specific exceptions
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final JwtProperties jwtProperties;
    private final SecretKey key;
    // For jjwt 0.11.5, build the parser with just the signing key.
    // It will validate the signature using the algorithm associated with the key.
    private final JwtParser parser;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // 1. Create the SecretKey. Keys.hmacShaKeyFor determines the algorithm based on key length.
        //    For >= 512 bits, it should use HS512.
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        logger.info("JwtUtil: Secret key created. Key algorithm: {}", key.getAlgorithm());

        // 2. Build the parser. Just provide the signing key.
        //    jjwt 0.11.5 will validate the signature using the algorithm the key was generated for.
        this.parser = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build();
        logger.info("JwtUtil: Parser built. It will expect tokens signed with the key's algorithm.");
    }

    public String generateToken(String username) {
        // Explicitly sign with HS512. This must match the key's capability.
        // If your secret is shorter than 512 bits, use HS256 instead.
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                // --- Explicitly use HS512 for signing ---
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        logger.debug("Generated JWT token for user: {}", username);
        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            return parser.parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) { // Catch specific JWT exceptions
            logger.warn("Failed to extract username from token.", e);
            throw e; // Re-throw to be handled by the caller
        }
    }

    // --- Improved validateToken signature and error handling ---
    // It's better practice to validate the username as well.
    public boolean validateToken(String token, String expectedUsername) {
        try {
            // 1. Parse the token (this checks signature and expiration)
            Claims claims = parser.parseClaimsJws(token).getBody();

            // 2. Check if the username in the token matches the expected one
            String usernameFromToken = claims.getSubject();
            boolean usernameValid = usernameFromToken.equals(expectedUsername);

            // 3. Check if the token is expired (parser does this, but double-check)
            boolean notExpired = !claims.getExpiration().before(new Date());

            boolean isValid = usernameValid && notExpired;
            if (!isValid) {
                logger.warn("Token validation failed. Username valid: {}, Not expired: {}", usernameValid, notExpired);
            } else {
                logger.debug("Token validated successfully for user: {}", expectedUsername);
            }
            return isValid;
        } catch (ExpiredJwtException eje) {
            logger.warn("JWT Token expired for user: {}", expectedUsername, eje);
            return false;
        } catch (SignatureException se) {
             // This is the specific exception for signature mismatch
             logger.warn("JWT Token signature validation failed for user: {}", expectedUsername, se);
             return false;
        } catch (JwtException e) { // Catch other JWT parsing errors (malformed, etc.)
            logger.warn("JWT Token validation failed (structure/claims) for user: {}", expectedUsername, e);
            return false;
        } catch (Exception e) { // Catch any other unexpected errors
            logger.error("Unexpected error during JWT token validation for user: {}", expectedUsername, e);
            return false;
        }
    }

    // Keep the old signature for compatibility if needed by other parts of your code,
    // but it's less secure as it doesn't check the username.
    public boolean validateToken(String token) {
        logger.warn("validateToken(String) called without username check. This is less secure.");
        try {
            parser.parseClaimsJws(token); // This checks signature and expiration
            logger.debug("Token validated successfully (signature/expiration only).");
            return true;
        } catch (ExpiredJwtException eje) {
            logger.warn("JWT Token expired (signature/expiration check).", eje);
            return false;
        } catch (SignatureException se) {
             // This is the specific exception for signature mismatch
             logger.warn("JWT Token signature validation failed (signature/expiration check).", se);
             return false;
        } catch (JwtException e) {
            logger.warn("JWT Token validation failed (signature/expiration) in simplified method.", e);
            return false;
        }
    }
}