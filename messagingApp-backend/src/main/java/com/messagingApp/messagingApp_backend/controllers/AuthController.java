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
    public ResponseEntity<?> getLoggedInUser(HttpSession session) {
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

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userDetails) {
        String username = userDetails.get("username");
        String password = userDetails.get("password");
        String role = userDetails.get("role");

        System.out.println("Received registration request: " + username + " | " + password + " | " + role);
        int rowsAffected = authService.registerUser(username, password, role);
        if (rowsAffected == 2) {
            return ResponseEntity.ok(Map.of("message", "Registration successful"));
        } else if (rowsAffected == 1) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid Role"));
        }
        return ResponseEntity.status(409).body(Map.of("error", "Error creating user"));
    }

}
