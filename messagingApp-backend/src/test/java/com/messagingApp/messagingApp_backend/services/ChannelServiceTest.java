package com.messagingApp.messagingApp_backend.services;

import com.messagingApp.messagingApp_backend.models.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties") // Use h2-test.properties for testing (H2 in-memory database)
public class ChannelServiceTest {
    // Tests for ChannelService class using H2 in-memory database

    @Autowired
    private ChannelService channelService;

    @Test
    public void testGetAllChannels() {
        // Given
        channelService.createChannel("General", "adminUser");

        // When
        List<Channel> channels = channelService.getAllChannels();

        // Then
        assert(channels.size() == 1);
        assert(channels.get(0).getName().equals("General"));
    }
}
