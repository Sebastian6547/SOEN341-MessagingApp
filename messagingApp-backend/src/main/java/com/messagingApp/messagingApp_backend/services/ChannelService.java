package com.messagingApp.messagingApp_backend.services;

import com.messagingApp.messagingApp_backend.models.Channel;
import com.messagingApp.messagingApp_backend.models.Message;
import com.messagingApp.messagingApp_backend.models.User;
import org.springframework.stereotype.Service;
import com.messagingApp.messagingApp_backend.services.AuthService;

import java.time.LocalDateTime;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.*;
import java.sql.*;


@Service
public class ChannelService {
    // This class is a service class that provides methods to get channels, users in a channel, messages in a channel, and the latest message in a channel
    // These methods are used by the channel controller to get data from the service layer
    // Load .env variables
    private static final Dotenv dotenv = Dotenv.load();

    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String DB_USER = dotenv.get("DB_USER");
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    // Get all channels
    public List<Channel> getAllChannels() {
        System.out.println("Fetching all channels from the database...");

        List<Map<String,Object>> result = executeQuery("SELECT * FROM channels");
        List<Channel> channels = new ArrayList<>();
        for (Map<String,Object> row : result){
            Channel channel = new Channel((String)row.get("name"), Channel.ChannelType.valueOf((String)row.get("type")));
            channels.add(channel);
        }
        return channels;
    }

    // Get all channels for a user
    public List<Channel> getUserChannels(String username) {
        //System.out.println("Fetching all channels for user: " + username + " from the database...");
        String query = """
        SELECT uc.username, uc.channel_name, c.type
        FROM user_channel uc
        JOIN channels c ON uc.channel_name = c.name
        WHERE uc.username = ?
        """;
        List<Map<String,Object>> result = executeQuery(query, username);
        List<Channel> channels = new ArrayList<>();
        for (Map<String,Object> row : result){
            Channel channel = new Channel((String)row.get("channel_name"), Channel.ChannelType.valueOf((String)row.get("type")));
            channels.add(channel);
        }
        return channels;
    }

    // Get all users in a channel
    public List<User> getUsersInChannel(String channelName) {
        //System.out.println("Fetching all users in channel: " + channelName + " from the database...");
        String query = """
        SELECT uc.username, u.role
        FROM user_channel uc
        JOIN users u ON uc.username = u.username
        WHERE uc.channel_name = ?
        """;
        List<Map<String,Object>> result = executeQuery(query, channelName);
        List<User> users = new ArrayList<>();
        for (Map<String,Object> row : result){
            User user = new User((String)row.get("username"), null , User.UserRole.valueOf((String)row.get("role"))); // Password set to null because it is not needed
            users.add(user);
        }
        return users;
    }

    // Get all messages in a channel
    public List<Message> getMessagesInChannel(String channelName) {
        //System.out.println("Fetching all messages in channel: " + channelName + " from the database...");
        // Query gets messages from a specific channel with only the username and role of the sender
        String query = """
        SELECT m.id, m.text, m.date_time, m.channel_name, u.username, u.role
        FROM messages m
        JOIN users u ON m.username = u.username
        WHERE m.channel_name = ?
        """;
        List<Map<String,Object>> result = executeQuery(query, channelName);
        List<Message> messages = new ArrayList<>();
        for (Map<String, Object> row : result) {
            User sender = new User(
                    (String) row.get("username"),
                    null, // Password set to null because it is not needed
                    User.UserRole.valueOf((String) row.get("role"))
            );

            Message message = new Message(
                    (int) row.get("id"),
                    (String) row.get("text"),
                    sender,
                    new Channel((String) row.get("channel_name"), null), // Channel type set to null because it is not needed
                    ((java.sql.Timestamp) row.get("date_time")).toLocalDateTime()
            );
            messages.add(message);
        }
        return messages;
    }


    // Get the latest message in a channel
    public Message getLatestMessageInChannel(String channelName) {
        // Query gets the latest message from a specific channel with only the username and role of the sender
        String query = """
        SELECT m.id, m.text, m.date_time, m.channel_name, u.username, u.role
        FROM messages m
        JOIN users u ON m.username = u.username
        WHERE m.channel_name = ?
        ORDER BY m.date_time DESC
        LIMIT 1
        """;
        List<Map<String,Object>> result = executeQuery(query, channelName);
        if (result.isEmpty()) {
            return null;
        }

        Map<String, Object> row = result.get(0);
        User sender = new User(
                (String) row.get("username"),
                null, // Password set to null because it is not needed
                User.UserRole.valueOf((String) row.get("role"))
        );

        return new Message(
                (int) row.get("id"),
                (String) row.get("text"),
                sender,
                new Channel((String) row.get("channel_name"), null), // Channel type set to null because it is not needed
                ((java.sql.Timestamp) row.get("date_time")).toLocalDateTime()
        );
    }

    //Get all users
    public List<User> getAllUsers() {
        System.out.println("Fetching all users from the database...");
        List<Map<String,Object>> result = executeQuery("SELECT * FROM users");
        List<User> users = new ArrayList<>();
        for (Map<String,Object> row : result){
            User user = new User((String)row.get("username"), (String)row.get("password"), User.UserRole.valueOf((String)row.get("role")));
            users.add(user);
        }
        return users;
    }

    // Get all users with matching string
    public List<User> findUser (String input) {
        System.out.println("Fetching all users matching string " + input );

        String query = """
        SELECT u.username, u.role
        FROM users u
        WHERE username ILIKE ?
        """;
        String search = "%" + input + "%";
        List<Map<String,Object>> result = executeQuery(query, search);
        List<User> users = new ArrayList<>();
        for (Map<String,Object> row : result){
            User user = new User((String)row.get("username"), null , User.UserRole.valueOf((String)row.get("role"))); // Password set to null because it is not needed
            users.add(user);
        }
        return users;
    }

    // Send a message to a channel
    public void sendMessage(String channelName, String content, String sender) {
        System.out.println("Sending message to channel: " + channelName + " from user: " + sender + " with content: " + content);
        String query = "INSERT INTO messages (text, username, channel_name, date_time) VALUES (?, ?, ?, ?)";
        executeUpdate(query, "Error sending message to channel", content, sender, channelName, Timestamp.valueOf(LocalDateTime.now()));

    }

    // Default method to get data from the database
    private List<Map<String,Object>> executeQuery(String query, Object... params){
        List<Map<String,Object>> result = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = connection.prepareStatement(query)){
            //Set query parameters
            for (int i = 0; i < params.length; i++){
                ps.setObject(i+1, params[i]);
            }

            //Execute the query
            try (ResultSet rs = ps.executeQuery()){
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()){
                    Map<String,Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++){
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }

        }catch (SQLException e){
            System.out.println("Database error during query execution: [" + query + "] with " + Arrays.toString(params));
            e.printStackTrace();
        }
        return result;
    }

    // Default method to update data in the database
    private int executeUpdate(String query, String errorMessage, Object... params) {
        int rowsAffected = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = connection.prepareStatement(query)) {

            // Bind parameters
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            // Execute update and return affected rows
            rowsAffected = ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println(errorMessage);
            e.printStackTrace();
        }

        return rowsAffected;
    }
    // Creating a channel
    public int createDMChannel(String channelName, String user1, String user2) {
        if (user1 == null) {
            System.out.println("Error: No user logged in");
            return -1;
        }
        if (user2 == null) {
            System.out.println("Error: No user selected");
            return -1;
        }
        System.out.println("Attempting to create channel: " + channelName + " with user: " + user1);
        // Insert the new channel into the database
        String createChannelQuery = "INSERT INTO channels (name, type) VALUES (?, 'DM')";
        int rowsAffected = executeUpdate(createChannelQuery, "Error inserting channel", channelName);

        // If channel creation failed, don't continue
        if (rowsAffected <= 0) {
            System.out.println("Failed to create channel: " + channelName);
            return -1;
        }

        // Insert the users into the user_channel table
        String addUser1ToChannelQuery = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";
        executeUpdate(addUser1ToChannelQuery, "Error adding user to channel", user1, channelName);

        String addUser2ToChannelQuery = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";
        executeUpdate(addUser2ToChannelQuery, "Error adding user to channel", user2, channelName);

        System.out.println("DM Channel created successfully added both users: " + user1+ " and " + user2);
        return rowsAffected;
    }

}
