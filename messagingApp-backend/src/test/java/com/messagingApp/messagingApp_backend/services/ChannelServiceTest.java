package com.messagingApp.messagingApp_backend.services;

import com.messagingApp.messagingApp_backend.models.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChannelServiceTest {
    // Tests for ChannelService class using test database

    @Autowired
    private ChannelService channelService;

    @Autowired
    private AuthService authService; // Added for creating user

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
    public void testGetAllChannels() {
        // Given
        authService.createUser("Alice", "password", "Admin");
        channelService.createChannel("Help", "Alice");

        // When
        List<Channel> channels = channelService.getAllChannels();

        // Then
        assert(channels.size() == 2);
        assert(channels.get(0).getName().equals("General"));
        assert(channels.get(1).getName().equals("Help"));
    }

    @Test
    public void testCreateChannel() {
        // Given
        String channelName = "Help";
        String username = "Alice";
        authService.createUser(username, "password", "Admin");


        // When
        channelService.createChannel(channelName, username);

        // Then
        Integer countChannels = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM channels WHERE name = ?", Integer.class, channelName);

        assert(countChannels == 1);
    }
}
