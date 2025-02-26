package com.messagingApp.messagingApp_backend.services;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final ChannelService channelService;

    public AdminService(ChannelService channelService) {
        this.channelService = channelService;
    }

    // Checking if a user is an admin
    public boolean isAdmin(String username) {
        String query = "SELECT role FROM users WHERE username = ?";
        List<Map<String, Object>> result = channelService.executeQuery(query, username);

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

            return channelService.executeUpdate(query, errorMessage, newRole, targetUsername);
        } else {
            System.out.println("Permission denied: Only admins can update user roles.");
            return 0;
        }
    }


}
