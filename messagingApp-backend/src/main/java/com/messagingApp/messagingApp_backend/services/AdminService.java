package com.messagingApp.messagingApp_backend.services;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import java.sql.*;
import java.util.*;


@Service
public class AdminService {
    private final ChannelService channelService;

    public AdminService(ChannelService channelService) {
        this.channelService = channelService;
    }

    // Checking if a user is an admin
    public boolean isAdmin(String username) {
        String query = "SELECT role FROM users WHERE username = ?";
        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, username);

        if (!result.isEmpty()) {
            Object role = result.get(0).get("role");
            return role != null && role.toString().equalsIgnoreCase("admin");
        }

        return false;
    }

    // Changing the role of a user
    public int updateUserRole(String currentUsername, String targetUsername, String newRole) {
        if (isAdmin(currentUsername)) {
            String query = "UPDATE users SET role = ? WHERE username = ?";
            String errorMessage = "Error updating user role";

            return ServiceUtility.executeUpdate(query, errorMessage, newRole, targetUsername);
        } else {
            System.out.println("Permission denied: Only admins can update user roles.");
            return 0;
        }
    }

    public boolean deleteMessage(Long msgId) {
        System.out.println("Received message delete request for id " + msgId);
        System.out.println("User is an admin. Deleting this message.");
        String sql = "DELETE FROM messages WHERE id = ?";

        int rowsUpdated = ServiceUtility.executeUpdate(sql, "Error Deleting Message", msgId);
        if (rowsUpdated > 0) {
            System.out.println("Message deleted successfully.");
            return true;
        } else {
            System.out.println("No message found with ID: " + msgId);
            return false;
        }
    }
}

