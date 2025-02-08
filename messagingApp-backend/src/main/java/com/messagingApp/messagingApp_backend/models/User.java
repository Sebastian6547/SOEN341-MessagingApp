package com.messagingApp.messagingApp_backend.models;

import java.util.List;

public class User {
    public enum UserRole {
        MEMBER,
        ADMIN
    }
    private String username;
    private String password;
    private UserRole role;
    private List<Channel> channels;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
