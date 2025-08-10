package com.smartbudgeter.demo.controllers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional; // Add this import if not already present
import com.smartbudgeter.demo.config.GoogleConfig;
import com.smartbudgeter.demo.config.JwtUtil;
import com.smartbudgeter.demo.models.*;
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
import org.springframework.security.core.AuthenticationException;
import com.smartbudgeter.demo.repositories.*;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import com.smartbudgeter.demo.dto.*;
import java.time.LocalDateTime;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest registerRequest) {
        logger.info("Registration attempt for display name: {}", registerRequest.getDisplayName());
        try {
            // Check for existing user
            if (userRepository.existsByDisplayName(registerRequest.getDisplayName())) {
                logger.info("Registration failed: Display name already exists: {}", registerRequest.getDisplayName());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
            if (userRepository.existsByEmailAndIsDeletedFalse(registerRequest.getEmail())) {
                logger.info("Registration failed: Email already exists: {}", registerRequest.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }

            // Create and save new user
            User user = new User();
            user.setDisplayName(registerRequest.getDisplayName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setFamilyMembers(registerRequest.getFamilyMember() != null ? registerRequest.getFamilyMember() : 1);
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with ID: {}", savedUser.getId());

            String jwt = jwtUtil.generateToken(savedUser.getDisplayName());
            logger.debug("JWT generated for new user ID: {}", savedUser.getId());

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
            refreshToken.setUser(savedUser);
            refreshTokenRepository.save(refreshToken);
            logger.debug("Refresh token generated for new user ID: {}", savedUser.getId());

            AuthResponse authResponse = new AuthResponse(jwt, refreshToken.getToken(), savedUser.getId(), savedUser.getDisplayName());
            logger.info("Returning AuthResponse for newly registered user ID: {}", savedUser.getId());

            return ResponseEntity.ok(authResponse); 
        } catch (Exception e) {
            logger.error("Unexpected error during registration for display name: {}", registerRequest.getDisplayName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed due to an internal error.");
        }
    }
@PostMapping("/google-signin")
public ResponseEntity<?> googleSignin(@RequestBody GoogleSigninRequest request) {
    try {
        // Verify Google ID token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(googleConfig.getClientId()))
                .build();

        GoogleIdToken idToken = verifier.verify(request.getIdToken());

        if (idToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setDisplayName(email.split("@")[0]);
                    newUser.setEmail(email);
                    newUser.setPassword(passwordEncoder.encode("google-auth"));
                    return userRepository.save(newUser);
                });

        String jwt = jwtUtil.generateToken(user.getDisplayName());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(new AuthResponse(jwt, refreshToken.getToken()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error validating Google ID token");
    }
}
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Attempting login for display name: {}", loginRequest.getDisplayName());

            // 1. Authenticate the user using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getDisplayName(), 
                            loginRequest.getPassword()
                    )
            );

            // 2. If authentication is successful, find the user entity
            Optional<User> optionalUser = userRepository.findByDisplayNameAndIsDeletedFalse(loginRequest.getDisplayName());

            if (optionalUser.isEmpty()) {
                logger.error("User not found after successful authentication for display name: {}", loginRequest.getDisplayName());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User lookup failed after authentication");
            }

            User user = optionalUser.get();

            // 3. Generate JWT token
            String jwt = jwtUtil.generateToken(user.getDisplayName());
            logger.info("JWT token generated for user ID: {}", user.getId());

            // 4. Generate Refresh Token
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
            refreshToken.setUser(user); // Link the refresh token to the user
            refreshTokenRepository.save(refreshToken); // Save the refresh token
            logger.info("Refresh token generated and saved for user ID: {}", user.getId());

            // 5. Create and return the AuthResponse DTO including userId
            AuthResponse authResponse = new AuthResponse(jwt, refreshToken.getToken(), user.getId(), user.getDisplayName());
            logger.info("Login successful for user ID: {}", user.getId());

            return ResponseEntity.ok(authResponse);

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for display name: {}", loginRequest.getDisplayName(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid display name or password");
        } catch (Exception e) {
            logger.error("Unexpected error during signin for display name: {}", loginRequest.getDisplayName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login");
        }
    }
}