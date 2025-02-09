package com.messagingApp.messagingApp_backend.controllers;

import com.messagingApp.messagingApp_backend.services.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        System.out.println("Received login request: " + username + " | " + password);

        if (authService.authenticateUser(username, password, session)) {
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }

    // Check logged in user
    @GetMapping("/check")
    public ResponseEntity<?> getLoggedInUser (HttpSession session) {
        String loggedInUser = authService.getLoggedInUser(session);
        if (loggedInUser != null) {
            return ResponseEntity.ok(Map.of("username", loggedInUser));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }
    }

    // Logout endpoint (This is a post request because it modifies the server state and to make sure the user intentionally needs to click a button to logout)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

}
