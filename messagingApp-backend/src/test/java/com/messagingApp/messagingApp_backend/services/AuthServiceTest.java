package com.messagingApp.messagingApp_backend.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setupDatabase() {
        //Add General channel
        jdbcTemplate.execute("INSERT INTO channels (name, type) VALUES ('General', 'PC');");
    }

    @AfterEach
    public void cleanupDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE users, channels, messages RESTART IDENTITY CASCADE;");
    }

    @Test
    public void testCreateUser(){
        // Given
        String username = "Alice";
        String password = "password";
        String role = "Admin";

        // When
        authService.createUser(username, password, role);

        /// Then
        Integer countUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        Integer countChannels = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_channel WHERE username = ?", Integer.class, username);

        assert(countUsers == 1);
        assert(countChannels == 1);
    }
}
