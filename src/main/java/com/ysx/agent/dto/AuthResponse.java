package com.ysx.agent.dto;

public class AuthResponse {

    private Long userId;

    private String token;

    private Long expiresIn;

    public AuthResponse() {
    }

    public AuthResponse(Long userId, String token, Long expiresIn) {
        this.userId = userId;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
