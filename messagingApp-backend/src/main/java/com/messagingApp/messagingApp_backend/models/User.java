package com.messagingApp.messagingApp_backend.models;

import java.util.List;

public class User {
    private final String username;
    private final String password;
    private final UserRole role;
    private List<Channel> channels;
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public void addChannel(Channel channel) {
        this.channels.add(channel);
    }

    public void removeChannel(Channel channel) {
        this.channels.remove(channel);
    }

    public boolean hasChannel(Channel channel) {
        return this.channels.contains(channel);
    }

    public boolean hasChannel(String channelName) {
        return this.channels.stream().anyMatch(channel -> channel.getName().equals(channelName));
    }

    public enum UserRole {
        MEMBER, ADMIN
    }
}
