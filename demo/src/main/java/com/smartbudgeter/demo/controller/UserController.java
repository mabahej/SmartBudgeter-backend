package com.smartbudgeter.demo.controller;

import com.smartbudgeter.demo.dto.UserResponse;
import com.smartbudgeter.demo.dto.UserUpdateRequest;
import com.smartbudgeter.demo.dto.PasswordUpdateRequest;
import com.smartbudgeter.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // Get all active users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    // Get all deleted users (admin only)
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllDeletedUsers() {
        List<UserResponse> users = userService.getAllDeletedUsers();
        return ResponseEntity.ok(users);
    }
    
    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        Optional<UserResponse> user = userService.getUserById(id);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update user information
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, 
                                       @Validated @RequestBody UserUpdateRequest request) {
        try {
            Optional<UserResponse> updatedUser = userService.updateUser(id, request);
            
            if (updatedUser.isPresent()) {
                return ResponseEntity.ok(updatedUser.get());
            }
            return ResponseEntity.notFound().build();
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Update user password
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, 
                                          @Validated @RequestBody PasswordUpdateRequest request) {
        try {
            boolean updated = userService.updatePassword(id, request);
            
            if (updated) {
                return ResponseEntity.ok("Password updated successfully");
            }
            return ResponseEntity.notFound().build();
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Soft delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.softDeleteUser(id);
        
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
    
    // Restore soft-deleted user (admin only)
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> restoreUser(@PathVariable Long id) {
        boolean restored = userService.restoreUser(id);
        
        if (restored) {
            return ResponseEntity.ok("User restored successfully");
        }
        return ResponseEntity.notFound().build();
    }
    
    // Search users
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String q) {
        List<UserResponse> users = userService.searchUsers(q);
        return ResponseEntity.ok(users);
    }
}
