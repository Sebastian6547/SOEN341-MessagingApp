package com.messagingApp.messagingApp_backend.services;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthServiceTest {

    private final String sampleMemberName = "member";
    private final String sampleMemberPassword = "member";
    private final String sampleAdminName = "admin";
    private final String sampleAdminPassword = "admin";

    @Autowired
    private HttpSession session;
    @Autowired
    private AuthService authService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setupDatabase() {
        //Add General channel
        jdbcTemplate.execute("INSERT INTO channels (name, type) VALUES ('General', 'PC');");
        // Create a Mock Session
        session = new MockHttpServletRequest().getSession(true);
        // Add users
        // Insert Member User
        jdbcTemplate.execute("INSERT INTO users(username, password, role) VALUES ('member', 'member', 'MEMBER')");
        // Insert Admin User
        jdbcTemplate.execute("INSERT INTO users(username, password, role) VALUES ('admin', 'admin', 'ADMIN')");
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
    public void createUserAdmin() {
        // Given
        String username = "Alice";
        String password = "password";
        String role = "ADMIN";

        // When
        authService.createUser(username, password, role);

        /// Then
        Integer countUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        Integer countChannels = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_channel WHERE username = ?", Integer.class, username);

        assertEquals(1, countUsers);
        assertEquals(1, countChannels);
    }

    @Test
    public void createUserMember() {
        // Given
        String username = "Bob";
        String password = "password";
        String role = "MEMBER";

        // When
        authService.createUser(username, password, role);

        /// Then
        Integer countUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        Integer countChannels = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_channel WHERE username = ?", Integer.class, username);

        assertEquals(1, countUsers);
        assertEquals(1, countChannels);
    }

    @Test
    public void createUserDuplicate() {
        // Given
        String username = "Bob";
        String password = "password";
        String role = "MEMBER";
        // When
        authService.createUser(username, password, role);
        authService.createUser(username, password, role);
        /// Then
        Integer countUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        Integer countChannels = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_channel WHERE username = ?", Integer.class, username);
        assertEquals(1, countUsers);
        assertEquals(1, countChannels);
    }

    @Test
    void authenticateUserAdmin() {
        // Ensure no logged in user
        assertNull(session.getAttribute("loggedInUser"), "loggedInUser should be null");
        authService.authenticateUser(sampleAdminName, sampleAdminPassword, session);
        assertEquals(session.getAttribute("loggedInUser"), sampleAdminName, "Should return" + sampleAdminName);
    }

    @Test
    void authenticateUserMember() {
        // Ensure no logged in user
        assertNull(session.getAttribute("loggedInUser"), "loggedInUser should be null");
        authService.authenticateUser(sampleMemberName, sampleMemberPassword, session);
        assertEquals(session.getAttribute("loggedInUser"), sampleMemberName, "Should return" + sampleMemberName);
    }

    @Test
    void authenticateUserNoGivenUsername() {
        // Ensure no logged in user
        assertNull(session.getAttribute("loggedInUser"), "loggedInUser should be null");
        authService.authenticateUser("", sampleAdminPassword, session); // Wrong password
        assertNull(session.getAttribute("loggedInUser"), "Should have null loggedInUser");
    }

    @Test
    void authenticateUserWrongPassword() {
        // Ensure no logged in user
        assertNull(session.getAttribute("loggedInUser"), "loggedInUser should be null");
        authService.authenticateUser(sampleMemberName, sampleAdminPassword, session); // Wrong password
        assertNull(session.getAttribute("loggedInUser"), "Should have null loggedInUser");
    }

    @Test
    void getLoggedInUserMember() {
        // Set loggedInUser as sampleMemberName
        session.setAttribute("loggedInUser", sampleMemberName);
        assertEquals(sampleMemberName, session.getAttribute("loggedInUser"), "Should return" + sampleMemberName);
    }

    @Test
    void getLoggedInUserAdmin() {
        // Set loggedInUser as sampleAdminName
        session.setAttribute("loggedInUser", sampleAdminName);
        assertEquals(sampleAdminName, session.getAttribute("loggedInUser"), "Should return" + sampleAdminName);
    }

    @Test
    void logoutMember() {
        // Set loggedInUser as sampleMemberName
        session.setAttribute("loggedInUser", sampleMemberName);
        authService.logout(session);
        // If invalidated, it will throw Exception
        assertThrows(IllegalStateException.class, () -> session.getAttribute("loggedInUser"), "Should have been invalidated");
    }

    @Test
    void logoutAdmin() {
        // Set loggedInUser as sampleAdminName
        session.setAttribute("loggedInUser", sampleAdminName);
        authService.logout(session);
        // If invalidated, it will throw Exception
        assertThrows(IllegalStateException.class, () -> session.getAttribute("loggedInUser"), "Should have been invalidated");
    }

    @Test
    void logoutNotLoggedIn() {
        authService.logout(session);
        // If invalidated, it will throw Exception
        assertThrows(IllegalStateException.class, () -> session.getAttribute("loggedInUser"), "Should have been invalidated");
    }
}
