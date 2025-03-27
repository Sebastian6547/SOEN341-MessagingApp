package com.messagingApp.messagingApp_backend.services;

import com.messagingApp.messagingApp_backend.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    ChannelService channelService = new ChannelService();
    AdminService adminService = new AdminService(channelService);

    String adminName = "admin";
    String adminPassword = "admin";
    String adminRole = "ADMIN";
    String insertUser = "INSERT INTO users(username, password, role) VALUES (?, ?, ?)";
    String deleteUser = "DELETE FROM users WHERE username = ?";
    String insertMessage = "INSERT INTO messages(username, text, channel_name) VALUES (?, ?, ?)";
    String getMessageId = "SELECT id FROM messages WHERE username = ? AND text = ? AND channel_name = ?";
    String deleteMessage = "DELETE FROM messages WHERE username = ? AND text = ? AND channel_name = ?";

    @BeforeEach
    void setUp() {
        // Insert an admin to be used to be checked
        channelService.executeUpdate(insertUser,"Error inserting user", adminName, adminPassword, adminRole);
    }

    @AfterEach
    void tearDown() {
        channelService.executeUpdate(deleteUser, "Error in deleting user", adminName);
    }


    @Test
    void isAdminReturnTrueForAdmin() {
        String sampleAdminName = "SampleAdmin";
        try {
            // Insert Admin User
            channelService.executeUpdate(insertUser, "Error inserting user", sampleAdminName, "empty", "ADMIN");
            assertTrue(adminService.isAdmin(sampleAdminName), "Admin user should return true");

        } finally {
            // Ensure clean up even with assertion fails
            channelService.executeUpdate(deleteUser, "Error in deleting user", sampleAdminName);
        }
    }
    @Test
    void isAdminReturnFalseForMember() {
        String sampleMemberName = "SampleMember";
        try {
            // Insert Member User
            channelService.executeUpdate(insertUser, "Error inserting user", sampleMemberName, "empty", "MEMBER");
            assertFalse(adminService.isAdmin(sampleMemberName), "Member user should return false");

        } finally {
            // Ensure clean up even with assertion fails
            channelService.executeUpdate(deleteUser, "Error in deleting user", sampleMemberName);
        }
    }


    @Test
    void updateUserRoleFromAdminToAdmin() {
        String sampleUser = "SampleUser";
        try {
            // Insert Member User
            channelService.executeUpdate(insertUser, "Error inserting user", sampleUser, "empty", "ADMIN");
            assertTrue(adminService.isAdmin(sampleUser), "SampleUser is currently admin");
            // Change to Admin
            adminService.updateUserRole(adminName, sampleUser, "ADMIN");
            assertTrue(adminService.isAdmin(sampleUser), "SampleUser is still admin");
        } finally {
            // Ensure clean up even with assertion fails
            channelService.executeUpdate(deleteUser, "Error in deleting user", sampleUser);
        }
    }
    @Test
    void updateUserRoleFromAdminToMember() {
        String sampleUser = "SampleUser";
        try {
            // Insert Admin User
            channelService.executeUpdate(insertUser, "Error inserting user", sampleUser, "empty", "ADMIN");
            assertTrue(adminService.isAdmin(sampleUser), "SampleUser is currently admin");
            // Change to Member
            adminService.updateUserRole(adminName, sampleUser, "MEMBER");
            assertFalse(adminService.isAdmin(sampleUser), "SampleUser is now member");
        } finally {
            // Ensure clean up even with assertion fails
            channelService.executeUpdate(deleteUser, "Error in deleting user", sampleUser);
        }
    }
    @Test
    void updateUserRoleFromMemberToMember() {
        String sampleUser = "SampleUser";
        try {
            // Insert Member User
            channelService.executeUpdate(insertUser, "Error inserting user", sampleUser, "empty", "MEMBER");
            assertFalse(adminService.isAdmin(sampleUser), "SampleUser is currently member");
            // Change to Member
            adminService.updateUserRole(adminName, sampleUser, "MEMBER");
            assertFalse(adminService.isAdmin(sampleUser), "SampleUser is still member");
        } finally {
            // Ensure clean up even with assertion fails
            channelService.executeUpdate(deleteUser, "Error in deleting user", sampleUser);
        }
    }

    @Test
    void updateUserRoleFromMemberToAdmin() {
        String sampleUser = "SampleUser";
        try {
            // Insert Member User
            channelService.executeUpdate(insertUser, "Error inserting user", sampleUser, "empty", "MEMBER");
            assertFalse(adminService.isAdmin(sampleUser), "SampleUser is currently member");
            // Change to Admin
            adminService.updateUserRole(adminName, sampleUser, "ADMIN");
            assertTrue(adminService.isAdmin(sampleUser), "SampleUser is now admin");
        } finally {
            // Ensure clean up even with assertion fails
            channelService.executeUpdate(deleteUser, "Error in deleting user", sampleUser);
        }
    }

    @Test
    void updateUserRoleRejectNonAdmin(){
        String sampleUser = "SampleUser";
        try {
            // Insert Member User
            channelService.executeUpdate(insertUser, "Error inserting user", sampleUser, "empty", "MEMBER");
            // Using sampleUser as requester since they're member
            int rowAffected = adminService.updateUserRole("SampleUser", sampleUser, "MEMBER");
            assertFalse(rowAffected > 0, "Update should not executed when not admin request");
        } finally {
            // Ensure clean up even with assertion fails
            channelService.executeUpdate(deleteUser, "Error in deleting user", sampleUser);
        }
    }

    @Test
    void deleteMessageExistingMessage() {
        String sampleMessage = "SampleMessage";
        String targetChannel = "General";
        try {
            // Insert a sample message
            channelService.executeUpdate(insertMessage, "Error inserting message", adminName, sampleMessage, targetChannel);
            // Getting the inserted sample message id
            List<Map<String,Object>> results = channelService.executeQuery(getMessageId, adminName, sampleMessage, targetChannel);
            // Deleting the message
            assertTrue(adminService.deleteMessage((long)( (int) results.get(0).get("id"))), "Deleting message should return true");

            // Getting the result, should be empty
            results = channelService.executeQuery(getMessageId, adminName, sampleMessage, targetChannel);
            assertTrue(results.isEmpty(), "Message should have been deleted");
        } finally {
            // Just fail-safe in case function did not delete properly
            channelService.executeUpdate(deleteMessage, "Error in deleting message", adminName, sampleMessage, targetChannel);
        }

    }
    @Test
    void deleteMessageNonExistingMessage() {
        String sampleMessage = "SampleMessage";
        String targetChannel = "General";
        try {
            // Insert a sample message
            channelService.executeUpdate(insertMessage, "Error inserting message", adminName, sampleMessage, targetChannel);
            // Getting the inserted sample message id
            List<Map<String,Object>> results = channelService.executeQuery(getMessageId, adminName, sampleMessage, targetChannel);
            // Deleting the message
            assertTrue(adminService.deleteMessage((long)( (int) results.get(0).get("id"))), "Deleting message should return true");
            // Deleting the message again
            assertFalse(adminService.deleteMessage((long)( (int) results.get(0).get("id"))), "Deleting message should return false");
        } finally {
            // Just fail-safe in case function did not delete properly
            channelService.executeUpdate(deleteMessage, "Error in deleting message", adminName, sampleMessage, targetChannel);
        }

    }

}