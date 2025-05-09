package com.messagingApp.messagingApp_backend.models;

import java.time.LocalDateTime;

public class Message {
    private final long id;
    private final String content;
    private final User sender;
    private final Channel channel;
    private final LocalDateTime timestamp;

    public Message(long id, String content, User sender, Channel channel, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public Channel getChannel() {
        return channel;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
