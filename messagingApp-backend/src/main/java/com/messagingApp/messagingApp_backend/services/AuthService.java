package com.messagingApp.messagingApp_backend.services;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;

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
    public boolean authenticateUser(String username, String password, HttpSession session) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            session.setAttribute("loggedInUser", username); // Store username in session
            return true;
        }
        return false;
    }

    // Get logged in user
    public String getLoggedInUser(HttpSession session) {
        return (String) session.getAttribute("loggedInUser");
    }

    // Logout user
    public void logout(HttpSession session) {
        session.invalidate();
    }
}
