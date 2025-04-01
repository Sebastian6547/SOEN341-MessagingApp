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

    @Autowired
    private HttpSession session;

    @Autowired
    private AuthService authService;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private final String sampleMemberName = "member";
    private final String sampleMemberPassword = "member";
    private final String sampleAdminName = "admin";
    private final String sampleAdminPassword = "admin";


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
    public void createUserAdmin(){
        // Given
        String username = "Alice";
        String password = "password";
        String role = "ADMIN";

        // When
        authService.createUser(username, password, role);

        /// Then
        Integer countUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        Integer countChannels = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_channel WHERE username = ?", Integer.class, username);

        assertEquals(1, countUsers);
        assertEquals(1, countChannels);
    }

    @Test
    public void createUserMember(){
        // Given
        String username = "Bob";
        String password = "password";
        String role = "MEMBER";

        // When
        authService.createUser(username, password, role);

        /// Then
        Integer countUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        Integer countChannels = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_channel WHERE username = ?", Integer.class, username);

        assertEquals(1, countUsers);
        assertEquals(1, countChannels);
    }
    @Test
    public void createUserDuplicate(){
        // Given
        String username = "Bob";
        String password = "password";
        String role = "MEMBER";
        // When
        authService.createUser(username, password, role);
        authService.createUser(username, password, role);
        /// Then
        Integer countUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        Integer countChannels = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_channel WHERE username = ?", Integer.class, username);
        assertEquals(1, countUsers);
        assertEquals(1, countChannels);
    }
    @Test
    void authenticateUserAdmin() {
        assertNull(session.getAttribute("loggedInUser"));
        authService.authenticateUser(sampleAdminName,sampleAdminPassword,session);
        assertEquals(session.getAttribute("loggedInUser"), sampleAdminName, "Should return" + sampleAdminName);
    }
    @Test
    void authenticateUserMember() {
        assertNull(session.getAttribute("loggedInUser"));
        authService.authenticateUser(sampleMemberName,sampleMemberPassword,session);
        assertEquals(session.getAttribute("loggedInUser"), sampleMemberName, "Should return" + sampleMemberName);
    }
    @Test
    void authenticateUserNoGivenUsername() {
        assertNull(session.getAttribute("loggedInUser"));
        authService.authenticateUser("",sampleAdminPassword,session); // Wrong password
        assertNull(session.getAttribute("loggedInUser"), "Should have null loggedInUser");
    }
    @Test
    void authenticateUserWrongPassword() {
        assertNull(session.getAttribute("loggedInUser"));
        authService.authenticateUser(sampleMemberName,sampleAdminPassword,session); // Wrong password
        assertNull(session.getAttribute("loggedInUser"), "Should have null loggedInUser");
    }

    @Test
    void getLoggedInUserMember() {
        session.setAttribute("loggedInUser", sampleMemberName);
        assertEquals(sampleMemberName, session.getAttribute("loggedInUser"), "Should return" + sampleMemberName);
    }
    @Test
    void getLoggedInUserAdmin() {
        session.setAttribute("loggedInUser", sampleAdminName);
        assertEquals(sampleAdminName, session.getAttribute("loggedInUser"), "Should return" + sampleAdminName);
    }

    @Test
    void logoutMember() {
        session.setAttribute("loggedInUser", sampleMemberName);
        authService.logout(session);
        assertThrows(IllegalStateException.class, () -> session.getAttribute("loggedInUser"), "Should have been invalidated");
    }
    @Test
    void logoutAdmin() {
        session.setAttribute("loggedInUser", sampleAdminName);
        authService.logout(session);
        assertThrows(IllegalStateException.class, () -> session.getAttribute("loggedInUser"), "Should have been invalidated");
    }
    @Test
    void logoutNotLoggedIn() {
        authService.logout(session);
        assertThrows(IllegalStateException.class, () -> session.getAttribute("loggedInUser"), "Should have been invalidated");
    }
}
