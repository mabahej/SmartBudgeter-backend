package com.smartbudgeter.demo.controller;

import com.smartbudgeter.demo.config.GoogleConfig;
import com.smartbudgeter.demo.config.JwtUtil;
import com.smartbudgeter.demo.model.RefreshToken;
import com.smartbudgeter.demo.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import com.smartbudgeter.demo.repository.RefreshTokenRepository;
import com.smartbudgeter.demo.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import com.smartbudgeter.demo.dto.*;;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleConfig googleConfig;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;
    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest registerRequest) {
        // Check for existing username
      if (userRepository.findByUsernameAndIsDeletedFalse(registerRequest.getUsername()).isPresent()) {
    return ResponseEntity.status(409).body("Username already exists");
}
if (userRepository.findByEmailAndIsDeletedFalse(registerRequest.getEmail()).isPresent()) {
    return ResponseEntity.status(409).body("Email already exists");
}
        // Create and save new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
   @PostMapping("/signin")
public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
    try {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        String jwt = jwtUtil.generateToken(loginRequest.getUsername());
        
        User user = userRepository.findByUsernameAndIsDeletedFalse(loginRequest.getUsername())
    .orElseThrow(() -> new RuntimeException("User not found"));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(new AuthResponse(jwt, refreshToken.getToken()));
    } catch (AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
    }
}

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        String jwt = jwtUtil.generateToken(storedToken.getUser().getUsername());
        return ResponseEntity.ok(new AuthResponse(jwt, request.getRefreshToken()));
    }
@PostMapping("/google-signin")
public ResponseEntity<?> googleSignin(@RequestBody GoogleSigninRequest request) {
    try {
        // Log the received ID token from client
        System.out.println("Received idToken: " + request.getIdToken());

        // Log the expected audience (client ID)
        System.out.println("Verifier audience (clientId): " + googleConfig.getClientId());

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(googleConfig.getClientId()))
                .build();

        GoogleIdToken idToken = verifier.verify(request.getIdToken());

        // Log whether verification succeeded or not
        if (idToken == null) {
            System.out.println("Token verification returned null (invalid token)");
            return ResponseEntity.status(401).body("Invalid Google ID token");
        }

        System.out.println("Token verification succeeded");

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
    .orElseGet(() -> {
        User newUser = new User();
        newUser.setUsername(email.split("@")[0]);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode("google-auth"));
        return userRepository.save(newUser);
    });

        String jwt = jwtUtil.generateToken(user.getUsername());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(new AuthResponse(jwt, refreshToken.getToken()));

    } catch (Exception e) {
        System.out.println("Exception during token verification: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(401).body("Error validating Google ID token");
    }
}

}
