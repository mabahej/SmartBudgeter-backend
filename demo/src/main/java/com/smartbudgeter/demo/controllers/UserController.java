package com.smartbudgeter.demo.controllers;

import com.smartbudgeter.demo.models.User;
import com.smartbudgeter.demo.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository repository;

    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String username = authentication.getName(); 
        return repository.findByDisplayName(username);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyUser(Authentication authentication) {
        return getCurrentUser(authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyUser(@RequestBody User updatedUser, Authentication authentication) {
        return getCurrentUser(authentication).map(user -> {
            user.setDisplayName(updatedUser.getDisplayName());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword()); // Only if needed
            user.setFamilyName(updatedUser.getFamilyName());
            user.setFamilyMembers(updatedUser.getFamilyMembers());
            user.setGoogleId(updatedUser.getGoogleId());

            return ResponseEntity.ok(repository.save(user));
        }).orElse(ResponseEntity.status(401).build());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Object> deleteMyUser(Authentication authentication) {
        return getCurrentUser(authentication).map(user -> {
            repository.delete(user);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance(Authentication authentication) {
        return getCurrentUser(authentication)
                .map(user -> ResponseEntity.ok(user.getBalance()))
                .orElse(ResponseEntity.status(401).build());
    }

    @PutMapping("/balance")
    public ResponseEntity<User> updateBalance(@RequestBody double newBalance, Authentication authentication) {
        return getCurrentUser(authentication).map(user -> {
            user.setBalance(newBalance);
            return ResponseEntity.ok(repository.save(user));
        }).orElse(ResponseEntity.status(401).build());
    }

    
    @GetMapping
    public ResponseEntity<?> allUsers(Authentication authentication) {
        return ResponseEntity.status(403).body("Access denied");
    }

    @PostMapping
    public ResponseEntity<?> saveUser() {
        return ResponseEntity.status(403).body("Use /auth/register instead");
    }
}
