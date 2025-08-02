package com.smartbudgeter.demo.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtParser;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final SecretKey key;
    private final JwtParser parser;
    
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Create SecretKey from the secret string
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        // Correct way to build the parser in jjwt 0.11.5+
        this.parser = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build();
    }


    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parser.parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parser.parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Log the exception if needed
            return false;
        }
    }
}
