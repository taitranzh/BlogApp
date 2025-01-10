package com.example.blogapp.entities;

public class UserInfo {
    private String email;
    private String userName;
    private String fullName;
    private String address;
    public UserInfo() {
    }
    public UserInfo(String email, String userName, String fullName, String address) {
        this.email = email;
        this.userName = userName;
        this.fullName = fullName;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
