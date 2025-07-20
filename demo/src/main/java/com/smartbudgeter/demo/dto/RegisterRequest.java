package com.smartbudgeter.demo.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class RegisterRequest {
    @NotBlank(message = "user name is required")
    @Size(min = 3, max = 20, message = "user name must be between 3 and 20 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 50, message = "Family name must not exceed 50 characters")
    private String familyName;

    private Integer familyMember;

    public String getUsername() {
        return userName;
    }

    public void setUsername(String displayName) {
        this.userName = displayName;
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

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Integer getFamilyMember() {
        return familyMember;
    }

    public void setFamilyMember(Integer familyMember) {
        this.familyMember = familyMember;
    }
}
