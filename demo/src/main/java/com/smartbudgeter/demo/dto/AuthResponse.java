package com.smartbudgeter.demo.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private int userId; // Use Long to match User entity ID type
    private String displayName; // Add this field

    // Constructor
    public AuthResponse(String accessToken, String refreshToken, int userId, String displayName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.displayName = displayName;
    }
    
    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters and setters...
     public int getUserId() {
         return userId;
     }

     public void setUserId(int userId) {
         this.userId = userId;
     }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}