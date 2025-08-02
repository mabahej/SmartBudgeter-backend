package com.smartbudgeter.demo.dto;

import java.time.LocalDateTime;

import com.smartbudgeter.demo.models.User;

public class UserResponse {
    private int id;
    private String username;
    private String email;
    private String familyName;
    private int familyMember;
    private LocalDateTime createdAt;
    private String updatedAt;
    private boolean isDeleted;
    
    // Constructor
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getDisplayName();
        this.email = user.getEmail();
        this.familyName = user.getFamilyName();
        this.familyMember = user.getFamilyMembers();
        this.createdAt = user.getCreatedAt();
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    
    public int getFamilyMember() { return familyMember; }
    public void setFamilyMember(int familyMember) { this.familyMember = familyMember; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
