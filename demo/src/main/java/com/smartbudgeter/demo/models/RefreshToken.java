package com.smartbudgeter.demo.models;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private Instant expiryDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public Instant getExpiryDate() {
        return expiryDate;
    }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
}