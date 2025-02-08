package com.messagingApp.messagingApp_backend.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final Map<String, String> users = new HashMap<>();

    public AuthService() {
        // Hardcoded users (username -> password)
        users.put("Alice", "password123");
        users.put("Bob", "password456");
        users.put("Charlie", "password789");
    }

    // Check if combination of username and password is valid
    public boolean authenticateUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}
