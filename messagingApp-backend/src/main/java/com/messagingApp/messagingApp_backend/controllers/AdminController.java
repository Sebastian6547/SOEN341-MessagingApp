package com.messagingApp.messagingApp_backend.controllers;

import com.messagingApp.messagingApp_backend.services.AdminService;
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
    @DeleteMapping("/deleteMessage/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId, HttpSession session)  {
        boolean isDeleted = adminService.deleteMessage(messageId, session);
        if (isDeleted) {
            System.out.println("Message deleted successfully");
            return ResponseEntity.ok("Message deleted successfully.");
        } else {
            return ResponseEntity.status(403).body("Something went wrong");
        }
    }

    @GetMapping("/checkAdmin")
    public ResponseEntity<Boolean> isAdmin(HttpSession session) {
        boolean isAdmin = adminService.isAdmin(session);
        return ResponseEntity.ok(isAdmin);
    }
}
