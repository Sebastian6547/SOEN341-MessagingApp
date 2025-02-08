package com.messagingApp.messagingApp_backend.services;

import com.messagingApp.messagingApp_backend.models.Channel;
import com.messagingApp.messagingApp_backend.models.Message;
import com.messagingApp.messagingApp_backend.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ChannelService {
    // This class is a service class that provides methods to get channels, users in a channel, messages in a channel, and the latest message in a channel
    // These methods are used by the channel controller to get data from the service layer

    // Get all channels
    public List<Channel> getAllChannels() {
        return Arrays.asList(
                new Channel("General", Channel.ChannelType.PC),
                new Channel("Project Help", Channel.ChannelType.PC),
                new Channel("Social", Channel.ChannelType.PC),
                new Channel("Alice_Bob_DM", Channel.ChannelType.DM) // Direct message channel
        );
    }

    // Get all channels for a user
    public List<Channel> getUserChannels(String username) {
        if (username.equals("Alice")) {
            return Arrays.asList(
                    new Channel("General", Channel.ChannelType.PC),
                    new Channel("Project Help", Channel.ChannelType.PC)
            );
        } else if (username.equals("Bob")) {
            return Arrays.asList(
                    new Channel("General", Channel.ChannelType.PC),
                    new Channel("Social", Channel.ChannelType.PC)
            );
        }
        return Arrays.asList(); // Return an empty list if user is not found
    }

    // Get all users in a channel
    public List<User> getUsersInChannel(String channelName) {
        if (channelName.equals("General")) {
            return Arrays.asList(
                    new User("Alice", "password123", User.UserRole.MEMBER),
                    new User("Bob", "password456", User.UserRole.MEMBER),
                    new User("Charlie", "password789", User.UserRole.ADMIN)
            );
        } else if (channelName.equals("Project Help")) {
            return Arrays.asList(
                    new User("Alice", "password123", User.UserRole.MEMBER),
                    new User("David", "password999", User.UserRole.ADMIN)
            );
        }
        return Arrays.asList();
    }

    // Get all messages in a channel
    public List<Message> getMessagesInChannel(String channelName) {
        if (channelName.equals("General")) {
            return Arrays.asList(
                    new Message(1L, "Hello everyone!", new User("Alice", "password123", User.UserRole.MEMBER), new Channel("General", Channel.ChannelType.PC), LocalDateTime.now().minusMinutes(5)),
                    new Message(2L, "Hi Alice!", new User("Bob", "password456", User.UserRole.MEMBER), new Channel("General", Channel.ChannelType.PC), LocalDateTime.now().minusMinutes(3)),
                    new Message(3L, "Welcome to the chat!", new User("Charlie", "password789", User.UserRole.ADMIN), new Channel("General", Channel.ChannelType.PC), LocalDateTime.now().minusMinutes(1))
            );
        } else if (channelName.equals("Project Help")) {
            return Arrays.asList(
                    new Message(4L, "Need help with Spring Boot?", new User("David", "password999", User.UserRole.ADMIN), new Channel("Project Help", Channel.ChannelType.PC), LocalDateTime.now().minusMinutes(2))
            );
        }
        return Arrays.asList();
    }


    // Get the latest message in a channel
    public Message getLatestMessageInChannel(String channelName) {
        if (channelName.equals("General")) {
            return new Message(3L, "Welcome to the chat!", new User("Charlie", "password789", User.UserRole.ADMIN), new Channel("General", Channel.ChannelType.PC), LocalDateTime.now().minusMinutes(1));
        } else if (channelName.equals("Project Help")) {
            return new Message(4L, "Need help with Spring Boot?", new User("David", "password999", User.UserRole.ADMIN), new Channel("Project Help", Channel.ChannelType.PC), LocalDateTime.now().minusMinutes(2));
        }
        return null;
    }
}
