package com.smartbudgeter.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "app_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;
    private String googleId;
    private String createdAt;
    private String updatedAt;
    private String family_name;
    private int family_member;

    private boolean isDeleted = false; // Soft delete flag
    private String deletedAt; // Timestamp for deletion

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<RefreshToken> refreshTokens;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now().toString();
        }
        this.updatedAt = LocalDateTime.now().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now().toString();
    }

    // Soft delete method
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now().toString();
        this.updatedAt = LocalDateTime.now().toString();
    }

    // Restore method (undo soft delete)
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.updatedAt = LocalDateTime.now().toString();
    }

    // Update user information method
    public void updateUserInfo(String username, String email, String familyName, int familyMember) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username;
        }
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }
        if (familyName != null) {
            this.family_name = familyName;
        }
        if (familyMember > 0) {
            this.family_member = familyMember;
        }
        this.updatedAt = LocalDateTime.now().toString();
    }

    // Update password method
    public void updatePassword(String newPassword) {
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            this.password = newPassword;
            this.updatedAt = LocalDateTime.now().toString();
        }
    }

    // Update Google ID method
    public void updateGoogleId(String googleId) {
        this.googleId = googleId;
        this.updatedAt = LocalDateTime.now().toString();
    }

    // Check if user is active (not soft deleted)
    public boolean isActive() {
        return !this.isDeleted;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public int getFamily_member() {
        return family_member;
    }

    public void setFamily_member(int family_member) {
        this.family_member = family_member;
    }

    public List<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(List<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }
}