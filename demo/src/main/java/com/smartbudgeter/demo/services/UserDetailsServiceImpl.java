// src/main/java/com/smartbudgeter/demo/services/UserDetailsServiceImpl.java
package com.smartbudgeter.demo.services;
import com.smartbudgeter.demo.models.User; // Your JPA User entity

import com.smartbudgeter.demo.repositories.UserRepository; // Your UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// Removed alias import; use fully qualified name in code below
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    // Constructor injection of UserRepository
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username/displayName: {}", username);

        // 1. Find your application's User entity by displayName
        // Make sure findByDisplayName exists in your UserRepository
        User userEntity = userRepository.findByDisplayName(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with display name: {}", username);
                    return new UsernameNotFoundException("User not found with display name: " + username);
                });

        logger.debug("User found: ID={}, DisplayName={}", userEntity.getId(), userEntity.getDisplayName());

        // 2. Define the user's authorities/roles
        // For simplicity, giving everyone the "USER" role.
        // You might fetch roles from your User entity or a separate Roles table.
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));

        // 3. Create and return a Spring Security UserDetails object
        // Using the built-in User class (org.springframework.security.core.userdetails.User).
        // - Pass the displayName as the 'username' for Spring Security context.
        // - Pass the hashed password from your User entity.
        // - Pass the authorities (roles).
        return new org.springframework.security.core.userdetails.User(
                userEntity.getDisplayName(), // This becomes the 'username' Spring Security uses (authentication.getName())
                userEntity.getPassword(),    // The hashed password
                authorities                  // The user's roles/permissions
        );
        // Important: The 'username' passed here (userEntity.getDisplayName()) must match
        // the value that your JwtUtil.getUsernameFromToken() extracts and that your
        // CategoryController.getCurrentUser uses (authentication.getName()).
    }
}