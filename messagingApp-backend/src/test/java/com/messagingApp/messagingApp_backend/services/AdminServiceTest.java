package com.messagingApp.messagingApp_backend.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminServiceTest {

    @Autowired
    ChannelService channelService = new ChannelService();

    @Autowired
    AdminService adminService = new AdminService(channelService);

    private final String sampleMemberName = "member";
    private final String sampleAdminName = "admin";
    String adminName = "admin";
    String getMessageId = "SELECT id FROM messages WHERE username = ? AND id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setupDatabase() {
        // Insert Member User
        jdbcTemplate.execute("INSERT INTO users(username, password, role) VALUES ('member', 'member', 'MEMBER')");
        // Insert Admin User
        jdbcTemplate.execute("INSERT INTO users(username, password, role) VALUES ('admin', 'admin', 'ADMIN')");
        // Inserting channel
        jdbcTemplate.execute("INSERT INTO channels (name, type) VALUES ('General', 'PC')");
        // Inserting user into channel
        jdbcTemplate.execute("INSERT INTO user_channel (username, channel_name) VALUES ('member', 'General')");
        jdbcTemplate.execute("INSERT INTO user_channel (username, channel_name) VALUES ('admin', 'General')");
        // Inserting sample message
        jdbcTemplate.execute("INSERT INTO public.messages (id, username, text, date_time, channel_name) VALUES (1, 'member', 'Remember to stay hydrated', '2025-03-29 14:05:23.254236', 'General')");
    }

    @AfterEach
    public void cleanupDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE users, channels, messages RESTART IDENTITY CASCADE;");
    }

    @Test
    void isAdminReturnTrueForAdmin() {
        // Using the sample admin from the @BeforeEach
        assertTrue(adminService.isAdmin(sampleAdminName), "Admin user should return true");
    }

    @Test
    void isAdminReturnFalseForMember() {
        assertFalse(adminService.isAdmin(sampleMemberName), "Member user should return false");
    }

    @Test
    void isAdminReturnFalseForNonExistingUser() {
        assertFalse(adminService.isAdmin("NoExist"), "Non-existing user should return false");
    }

    @Test
    void updateUserRoleFromMemberToMember() {
        assertFalse(adminService.isAdmin(sampleMemberName), "SampleUser is currently member");
        // Change to Member
        adminService.updateUserRole(adminName, sampleMemberName, "MEMBER");
        assertFalse(adminService.isAdmin(sampleMemberName), "SampleUser is still member");

    }

    @Test
    void updateUserRoleFromAdminToAdmin() {
        // Insert Member User
        assertTrue(adminService.isAdmin(sampleAdminName), "SampleUser is currently admin");
        // Change to Admin
        adminService.updateUserRole(adminName, sampleAdminName, "ADMIN");
        assertTrue(adminService.isAdmin(sampleAdminName), "SampleUser is still admin");
    }
    @Test
    void updateUserRoleFromAdminToMember() {
        // Check if currently Admin
        assertTrue(adminService.isAdmin(sampleAdminName), "SampleUser is currently admin");
        // Change to Member
        adminService.updateUserRole(adminName, sampleAdminName, "MEMBER");
        assertFalse(adminService.isAdmin(sampleAdminName), "SampleUser is now member");
    }


    @Test
    void updateUserRoleFromMemberToAdmin() {
        assertFalse(adminService.isAdmin(sampleMemberName), "SampleUser is currently member");
        // Change to Admin
        adminService.updateUserRole(adminName, sampleMemberName, "ADMIN");
        assertTrue(adminService.isAdmin(sampleMemberName), "SampleUser is now admin");
    }

    @Test
    void updateUserRoleRejectNonAdmin(){
        int rowAffected = adminService.updateUserRole(sampleMemberName, sampleMemberName, "MEMBER");
        assertFalse(rowAffected > 0, "Update should not executed when not admin request");
    }

    @Test
    void deleteMessageExistingMessage() {// Getting the inserted sample message id
        List<Map<String,Object>> results = ServiceUtility.executeQuery(getMessageId, sampleMemberName, 1);
        assertFalse(results.isEmpty(), "Message should exist");
        // Deleting the message
        assertTrue(adminService.deleteMessage(1L), "Deleting message should return true");
        // Getting the result, should be empty
        results = ServiceUtility.executeQuery(getMessageId, sampleMemberName, 1);
        assertTrue(results.isEmpty(), "Message should have been deleted");


    }
    @Test
    void deleteMessageNonExistingMessage() {
        List<Map<String,Object>> results = ServiceUtility.executeQuery(getMessageId, sampleMemberName, 1);
        assertTrue(adminService.deleteMessage(1L), "Deleting message should return true");
        // Deleting the message again
        assertFalse(adminService.deleteMessage(1L), "Deleting message should return false");
    }

}
