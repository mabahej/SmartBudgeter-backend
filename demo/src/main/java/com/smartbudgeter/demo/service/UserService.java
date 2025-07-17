package com.smartbudgeter.demo.service;

import com.smartbudgeter.demo.dto.UserResponse;
import com.smartbudgeter.demo.dto.UserUpdateRequest;
import com.smartbudgeter.demo.dto.PasswordUpdateRequest;
import com.smartbudgeter.demo.model.User;
import com.smartbudgeter.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findByIsDeletedFalse()
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<UserResponse> getAllDeletedUsers() {
        return userRepository.findByIsDeletedTrue()
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
    
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .map(UserResponse::new);
    }
    
    public Optional<UserResponse> updateUser(Long id, UserUpdateRequest request) {
        Optional<User> userOpt = userRepository.findByIdAndIsDeletedFalse(id);
        
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Check if username is already taken by another user
        if (request.getUsername() != null && 
            !request.getUsername().equals(user.getUsername()) && 
            userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email is already taken by another user
        if (request.getEmail() != null && 
            !request.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new RuntimeException("Email already exists");
        }
        
        // Update user information
        user.updateUserInfo(
            request.getUsername(),
            request.getEmail(),
            request.getFamilyName(),
            request.getFamilyMember() != null ? request.getFamilyMember() : user.getFamily_member()
        );
        
        User savedUser = userRepository.save(user);
        return Optional.of(new UserResponse(savedUser));
    }
    
    public boolean updatePassword(Long id, PasswordUpdateRequest request) {
        Optional<User> userOpt = userRepository.findByIdAndIsDeletedFalse(id);
        
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return true;
    }
    
    public boolean softDeleteUser(Long id) {
        Optional<User> userOpt = userRepository.findByIdAndIsDeletedFalse(id);
        
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        user.softDelete();
        userRepository.save(user);
        return true;
    }
    
    public boolean restoreUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent() || !userOpt.get().isDeleted()) {
            return false;
        }
        
        User user = userOpt.get();
        user.restore();
        userRepository.save(user);
        return true;
    }
    
    public List<UserResponse> searchUsers(String searchTerm) {
        return userRepository.findActiveUsersBySearch(searchTerm)
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
}