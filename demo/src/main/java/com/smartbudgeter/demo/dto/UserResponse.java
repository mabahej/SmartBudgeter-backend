package com.smartbudgeter.demo.dto;

import com.smartbudgeter.demo.model.User;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String familyName;
    private int familyMember;
    private String createdAt;
    private String updatedAt;
    private boolean isDeleted;
    
    // Constructor
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.familyName = user.getFamily_name();
        this.familyMember = user.getFamily_member();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.isDeleted = user.isDeleted();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    
    public int getFamilyMember() { return familyMember; }
    public void setFamilyMember(int familyMember) { this.familyMember = familyMember; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
