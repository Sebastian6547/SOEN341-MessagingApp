package com.messagingApp.messagingApp_backend.controllers;

import com.messagingApp.messagingApp_backend.services.AdminService;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Checking if a user is an admin
    @GetMapping("/checkAdmin")
    public ResponseEntity<Boolean> isAdmin(String username) {
        boolean isAdmin = adminService.isAdmin(username);
        return ResponseEntity.ok(isAdmin);
    }

    // Changing the role of a user
    @PutMapping("/updateRole")
    public ResponseEntity<String> updateUserRole(
            @RequestParam String currentUsername,
            @RequestParam String targetUsername,
            @RequestParam String newRole) {

        int updatedRows = adminService.updateUserRole(currentUsername, targetUsername, newRole);

        if (updatedRows > 0) {
            return ResponseEntity.ok("User role updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Permission denied or failed to update user role.");
        }
    }

    @DeleteMapping("/deleteMessage/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId)  {
        boolean isDeleted = adminService.deleteMessage(messageId);
        if (isDeleted) {
            System.out.println("Message deleted successfully");
            return ResponseEntity.ok("Message deleted successfully.");
        } else {
            return ResponseEntity.status(403).body("Something went wrong");
        }
    }

    }
