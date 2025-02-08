package com.messagingApp.messagingApp_backend.models;

import java.util.List;

public class Channel {
    public enum ChannelType {
        PC, // Public Channel
        DM  // Direct Message
    }

    private String name;
    private ChannelType type;
    private List<User> members;

    public Channel(String name, ChannelType type) {
        this.name = name;
        this.type = type;
    }
}
