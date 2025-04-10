package com.messagingApp.messagingApp_backend.models;

import java.util.List;

public class Channel {
    private final String name;
    private final ChannelType type;
    private List<User> members;

    public Channel(String name, ChannelType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ChannelType getType() {
        return type;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public void addMember(User user) {
        this.members.add(user);
    }

    public void removeMember(User user) {
        this.members.remove(user);
    }

    public enum ChannelType {
        PC, // Public Channel
        DM  // Direct Message
    }
}
