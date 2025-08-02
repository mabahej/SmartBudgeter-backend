package com.smartbudgeter.demo.models;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "family_members")
    private int familyMembers;
    @Column(name = "balance")

    private double balance;
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reminder> reminders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Expense> expenses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Budget> budgets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<RefreshToken> refreshTokens;
    public User() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public int getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(int familyMembers) {
        this.familyMembers = familyMembers;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
     @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Soft delete method
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Restore method (undo soft delete)
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.updatedAt = LocalDateTime.now();
    }
     public void updatePassword(String newPassword) {
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            this.password = newPassword;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Update Google ID method
    public void updateGoogleId(String googleId) {
        this.googleId = googleId;
        this.updatedAt = LocalDateTime.now();
    }

    // Check if user is active (not soft deleted)
    public boolean isActive() {
        return !this.isDeleted;
    }
    public LocalDateTime getdeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
}
/*users {
  id string pk
  email string
  password string
  google_id string
  display_name string
  created_at timestamp
  family_name string
  family_member int
}
 */