// 1. DTOs (Data Transfer Objects)
// UserUpdateRequest.java
package com.smartbudgeter.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String familyName;
    
    private Integer familyMember;
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    
    public Integer getFamilyMember() { return familyMember; }
    public void setFamilyMember(Integer familyMember) { this.familyMember = familyMember; }
}