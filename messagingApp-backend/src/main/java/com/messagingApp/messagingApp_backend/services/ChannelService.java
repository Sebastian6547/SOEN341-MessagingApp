package com.messagingApp.messagingApp_backend.services;

import com.messagingApp.messagingApp_backend.models.Channel;
import com.messagingApp.messagingApp_backend.models.Message;
import com.messagingApp.messagingApp_backend.models.User;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class ChannelService {
    // This class is a service class that provides methods to get channels, users in a channel, messages in a channel, and the latest message in a channel
    // These methods are used by the channel controller to get data from the service layer
    // Load .env variables

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String DB_URL = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : dotenv.get("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : dotenv.get("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : dotenv.get("DB_PASSWORD");

    static {
        System.out.println("Loaded DB_URL: " + (DB_URL != null && !DB_URL.isEmpty() ? "✔" : "❌"));
        System.out.println("Loaded DB_USER: " + (DB_USER != null && !DB_USER.isEmpty() ? "✔" : "❌"));
        System.out.println("Loaded DB_PASSWORD: " + (DB_PASSWORD != null && !DB_PASSWORD.isEmpty() ? "✔" : "❌"));
    }

    // Get all channels
    public List<Channel> getAllChannels() {
        System.out.println("Fetching all channels from the database...");

        List<Map<String, Object>> result = ServiceUtility.executeQuery("SELECT * FROM channels");
        List<Channel> channels = new ArrayList<>();
        for (Map<String, Object> row : result) {
            Channel channel = new Channel((String) row.get("name"), Channel.ChannelType.valueOf((String) row.get("type")));
            channels.add(channel);
        }
        return channels;
    }

    // Get all channels for a user
    public List<Channel> getUserChannels(String username) {
        String query = """
            SELECT uc.username, uc.channel_name, c.type
            FROM user_channel uc
            JOIN channels c ON uc.channel_name = c.name
            WHERE uc.username = ?
            """;
        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, username);
        List<Channel> channels = new ArrayList<>();
        for (Map<String, Object> row : result) {
            Channel channel = new Channel((String) row.get("channel_name"), Channel.ChannelType.valueOf((String) row.get("type")));
            channels.add(channel);
        }
        return channels;
    }

    // Get all users in a channel
    public List<User> getUsersInChannel(String channelName) {
        String query = """
            SELECT uc.username, u.role
            FROM user_channel uc
            JOIN users u ON uc.username = u.username
            WHERE uc.channel_name = ?
            """;
        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, channelName);
        List<User> users = new ArrayList<>();
        for (Map<String, Object> row : result) {
            User user = new User((String) row.get("username"), null, User.UserRole.valueOf((String) row.get("role"))); // Password set to null because it is not needed
            users.add(user);
        }
        return users;
    }

    // Get all messages in a channel
    public List<Message> getMessagesInChannel(String channelName) {
        // Query gets messages from a specific channel with only the username and role of the sender
        String query = """
            SELECT m.id, m.text, m.date_time, m.channel_name, u.username, u.role
            FROM messages m
            JOIN users u ON m.username = u.username
            WHERE m.channel_name = ?
            """;
        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, channelName);
        List<Message> messages = new ArrayList<>();
        for (Map<String, Object> row : result) {
            User sender = new User((String) row.get("username"), null, // Password set to null because it is not needed
                User.UserRole.valueOf((String) row.get("role")));

            Message message = new Message((int) row.get("id"), (String) row.get("text"), sender, new Channel((String) row.get("channel_name"), null), // Channel type set to null because it is not needed
                ((java.sql.Timestamp) row.get("date_time")).toLocalDateTime());
            messages.add(message);
        }
        return messages;
    }

    // Get last seen messages
    public Long getLastSeenMsg(String username, String channelName) {
        String query = """
            SELECT last_read_msg_id
            FROM messages_seen
            WHERE username = ? AND channel_name = ?
            """;
        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, username, channelName);
        if (!result.isEmpty() && result.get(0).get("last_read_msg_id") != null) {
            return ((Number) result.get(0).get("last_read_msg_id")).longValue();
        }
        return 0L;
    }

    // Updating the message seen table
    public void updateMessageSeenTable(String username, String channelName, Long lastSeenMsgId) {
        String query = """
            INSERT INTO messages_seen (username, channel_name, last_read_msg_id)
            VALUES (?, ?, ?)
            ON CONFLICT (username, channel_name)
            DO UPDATE SET last_read_msg_id = EXCLUDED.last_read_msg_id;
            """;

        ServiceUtility.executeUpdate(query, "Error when updating messages_seen table", username, channelName, lastSeenMsgId);
    }

    // Get unread channels
    public List<String> getUnreadChannels(String username) {

        String query = """
            SELECT DISTINCT m.channel_name
            FROM messages m
            LEFT JOIN messages_seen ms ON m.channel_name = ms.channel_name AND ms.username = ?
            WHERE ms.last_read_msg_id IS NULL OR m.id > ms.last_read_msg_id;
            """;

        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, username);

        List<String> unreadChannels = new ArrayList<>();
        for (Map<String, Object> row : result) {
            unreadChannels.add((String) row.get("channel_name"));
        }
        return unreadChannels;
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
        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, channelName);
        if (result.isEmpty()) {
            return null;
        }

        Map<String, Object> row = result.get(0);
        User sender = new User((String) row.get("username"), null, // Password set to null because it is not needed
            User.UserRole.valueOf((String) row.get("role")));

        return new Message((int) row.get("id"), (String) row.get("text"), sender, new Channel((String) row.get("channel_name"), null), // Channel type set to null because it is not needed
            ((java.sql.Timestamp) row.get("date_time")).toLocalDateTime());
    }

    //Get all users
    public List<User> getAllUsers() {
        System.out.println("Fetching all users from the database...");
        List<Map<String, Object>> result = ServiceUtility.executeQuery("SELECT * FROM users");
        List<User> users = new ArrayList<>();
        for (Map<String, Object> row : result) {
            User user = new User((String) row.get("username"), (String) row.get("password"), User.UserRole.valueOf((String) row.get("role")));
            users.add(user);
        }
        return users;
    }

    // Get all users with matching string
    public List<User> findUser(String input) {
        System.out.println("Fetching all users matching string " + input);

        String query = """
            SELECT u.username, u.role
            FROM users u
            WHERE LOWER(username) LIKE LOWER(?)
            """;
        String search = "%" + input + "%";
        List<Map<String, Object>> result = ServiceUtility.executeQuery(query, search);
        List<User> users = new ArrayList<>();
        for (Map<String, Object> row : result) {
            User user = new User((String) row.get("username"), null, User.UserRole.valueOf((String) row.get("role"))); // Password set to null because it is not needed
            users.add(user);
        }
        return users;
    }

    // Get the number of admins in a channel
    public int getAdminsCountForChannel(String channelName) {
        System.out.println("Fetching the number of admins in channel: " + channelName + " from the database...");

        List<User> users = getUsersInChannel(channelName);

        int adminsCount = 0;
        for (User user : users) {
            if (user.getRole() == User.UserRole.ADMIN) {
                adminsCount++;
            }
        }

        return adminsCount;
    }

    // Send a message to a channel
    public void sendMessage(String channelName, String content, String sender) {
        System.out.println("Sending message to channel: " + channelName + " from user: " + sender + " with content: " + content);
        String query = "INSERT INTO messages (text, username, channel_name, date_time) VALUES (?, ?, ?, ?)";
        ServiceUtility.executeUpdate(query, "Error sending message to channel", content, sender, channelName, Timestamp.valueOf(LocalDateTime.now()));

    }

    // Creating a channel
    public int createChannel(String channelName, String creatorUsername) {
        if (creatorUsername == null) {
            System.out.println("Error: No user logged in");
            return -1;
        }

        // Insert the new channel into the database
        String createChannelQuery = "INSERT INTO channels (name, type) VALUES (?, 'PC')";
        int rowsAffected = ServiceUtility.executeUpdate(createChannelQuery, "Error inserting channel", channelName);

        // If channel creation failed, don't continue
        if (rowsAffected <= 0) {
            return -1;
        }

        // Insert the creator into the user_channel table
        String addUserToChannelQuery = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";
        ServiceUtility.executeUpdate(addUserToChannelQuery, "Error adding user to channel", creatorUsername, channelName);

        System.out.println("Channel created successfully by user: " + creatorUsername);
        return rowsAffected;
    }

    public int deleteChannel(String channelName) {
        System.out.println("Trying to delete channel: " + channelName);

        // Check if the channel exists
        String checkChannelQuery = "SELECT COUNT(*) AS count FROM channels WHERE name = ?";
        List<Map<String, Object>> result = ServiceUtility.executeQuery(checkChannelQuery, channelName);

        // If no rows are found or count is 0 the channel doesn't exist
        if (result.isEmpty() || result.get(0).get("count") == null || ((Long) result.get(0).get("count")).intValue() <= 0) {
            System.out.println("Error: Channel does not exist");
            return -1;
        }

        // Delete the associated rows in the user_channel table (all user-channel associations)
        String deleteUserChannelQuery = "DELETE FROM user_channel WHERE channel_name = ?";
        int userChannelRowsAffected = ServiceUtility.executeUpdate(deleteUserChannelQuery, "Error deleting from user_channel", channelName);

        // If there's an issue deleting user-channel associations don't continue
        if (userChannelRowsAffected <= 0) {
            System.out.println("Error: Failed to delete user-channel associations");
            return -1;
        }

        // Delete the channel from the channels table
        String deleteChannelQuery = "DELETE FROM channels WHERE name = ?";
        int rowsAffected = ServiceUtility.executeUpdate(deleteChannelQuery, "Error deleting channel", channelName);

        if (rowsAffected <= 0) {
            System.out.println("Error: Failed to delete channel");
            return -1;
        }

        System.out.println("Channel deleted successfully: " + channelName);
        return rowsAffected;
    }

    // Joining a channel
    public boolean joinChannel(String channelName, String username) {
        System.out.println("Trying to join channel: " + channelName);
        String sql = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";
        int rowsAffected = ServiceUtility.executeUpdate(sql, "Error joining the server", username, channelName);
        if (rowsAffected <= 0) {
            System.out.println("Error: Failed to join channel");
            return false;
        }
        System.out.println(username + " joined channel successfully: " + channelName);
        return true;
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
        int rowsAffected = ServiceUtility.executeUpdate(createChannelQuery, "Error inserting channel", channelName);

        // If channel creation failed, don't continue
        if (rowsAffected <= 0) {
            System.out.println("Failed to create channel: " + channelName);
            return -1;
        }

        // Insert the users into the user_channel table
        String addUser1ToChannelQuery = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";
        ServiceUtility.executeUpdate(addUser1ToChannelQuery, "Error adding user to channel", user1, channelName);

        String addUser2ToChannelQuery = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";
        ServiceUtility.executeUpdate(addUser2ToChannelQuery, "Error adding user to channel", user2, channelName);

        System.out.println("DM Channel created successfully added both users: " + user1 + " and " + user2);
        return rowsAffected;
    }

}
