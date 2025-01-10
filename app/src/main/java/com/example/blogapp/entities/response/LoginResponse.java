package com.example.blogapp.entities.response;

import java.util.List;

public class LoginResponse {
    private String email;
    private TokenPair tokenPair;
    private List<String> roles;

    public String getEmail() {
        return email;
    }

    public TokenPair getTokenPair() {
        return tokenPair;
    }

    public List<String> getRoles() {
        return roles;
    }

    public static class TokenPair {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}

