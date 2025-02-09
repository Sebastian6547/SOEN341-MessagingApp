package com.messagingApp.messagingApp_backend.models;

import java.time.LocalDateTime;

public class Message {
    private long id;
    private String content;
    private User sender;
    private Channel channel;
    private LocalDateTime timestamp;

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
