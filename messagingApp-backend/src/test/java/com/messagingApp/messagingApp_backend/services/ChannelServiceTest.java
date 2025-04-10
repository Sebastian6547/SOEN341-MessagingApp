package com.messagingApp.messagingApp_backend.services;

import com.messagingApp.messagingApp_backend.models.Channel;
import com.messagingApp.messagingApp_backend.models.Message;
import com.messagingApp.messagingApp_backend.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ChannelServiceTest {

    @InjectMocks
    private ChannelService channelService;

    @BeforeEach
    public void setup() {
        // No setup required as we'll use static mocking for ServiceUtility
    }

    @Test
    public void testGetAllChannels() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("name", "General");
        row.put("type", "PC");
        resultData.add(row);

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq("SELECT * FROM channels"))).thenReturn(resultData);

            // Call the method under test
            List<Channel> channels = channelService.getAllChannels();

            // Verify the results
            assertEquals(1, channels.size());
            assertEquals("General", channels.get(0).getName());
            assertEquals(Channel.ChannelType.PC, channels.get(0).getType());
        }
    }

    @Test
    public void testGetUserChannels() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("username", "testUser");
        row.put("channel_name", "General");
        row.put("type", "PC");
        resultData.add(row);

        String query = """
            SELECT uc.username, uc.channel_name, c.type
            FROM user_channel uc
            JOIN channels c ON uc.channel_name = c.name
            WHERE uc.username = ?
            """;

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(query), eq("testUser"))).thenReturn(resultData);

            // Call the method under test
            List<Channel> channels = channelService.getUserChannels("testUser");

            // Verify the results
            assertEquals(1, channels.size());
            assertEquals("General", channels.get(0).getName());
            assertEquals(Channel.ChannelType.PC, channels.get(0).getType());
        }
    }

    @Test
    public void testGetUsersInChannel() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("username", "testUser");
        row.put("role", "MEMBER");
        resultData.add(row);

        String query = """
            SELECT uc.username, u.role
            FROM user_channel uc
            JOIN users u ON uc.username = u.username
            WHERE uc.channel_name = ?
            """;

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(query), eq("General"))).thenReturn(resultData);

            // Call the method under test
            List<User> users = channelService.getUsersInChannel("General");

            // Verify the results
            assertEquals(1, users.size());
            assertEquals("testUser", users.get(0).getUsername());
            assertEquals(User.UserRole.MEMBER, users.get(0).getRole());
        }
    }

    @Test
    public void testGetMessagesInChannel() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("text", "Hello World");
        row.put("date_time", new Timestamp(System.currentTimeMillis()));
        row.put("channel_name", "General");
        row.put("username", "testUser");
        row.put("role", "MEMBER");
        resultData.add(row);

        String query = """
            SELECT m.id, m.text, m.date_time, m.channel_name, u.username, u.role
            FROM messages m
            JOIN users u ON m.username = u.username
            WHERE m.channel_name = ?
            """;

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(query), eq("General"))).thenReturn(resultData);

            // Call the method under test
            List<Message> messages = channelService.getMessagesInChannel("General");

            // Verify the results
            assertEquals(1, messages.size());
            assertEquals("Hello World", messages.get(0).getContent());
            assertEquals("testUser", messages.get(0).getSender().getUsername());
            assertEquals("General", messages.get(0).getChannel().getName());
        }
    }

    @Test
    public void testGetLatestMessageInChannel() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("text", "Hello World");
        row.put("date_time", new Timestamp(System.currentTimeMillis()));
        row.put("channel_name", "General");
        row.put("username", "testUser");
        row.put("role", "MEMBER");
        resultData.add(row);

        String query = """
            SELECT m.id, m.text, m.date_time, m.channel_name, u.username, u.role
            FROM messages m
            JOIN users u ON m.username = u.username
            WHERE m.channel_name = ?
            ORDER BY m.date_time DESC
            LIMIT 1
            """;

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(query), eq("General"))).thenReturn(resultData);

            // Call the method under test
            Message message = channelService.getLatestMessageInChannel("General");

            // Verify the results
            assertNotNull(message);
            assertEquals("Hello World", message.getContent());
            assertEquals("testUser", message.getSender().getUsername());
            assertEquals("General", message.getChannel().getName());
        }
    }

    @Test
    public void testGetLatestMessageInChannelNoMessages() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();

        String query = """
            SELECT m.id, m.text, m.date_time, m.channel_name, u.username, u.role
            FROM messages m
            JOIN users u ON m.username = u.username
            WHERE m.channel_name = ?
            ORDER BY m.date_time DESC
            LIMIT 1
            """;

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(query), eq("EmptyChannel"))).thenReturn(resultData);

            // Call the method under test
            Message message = channelService.getLatestMessageInChannel("EmptyChannel");

            // Verify the results
            assertNull(message);
        }
    }

    @Test
    public void testGetAllUsers() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("username", "testUser");
        row.put("password", "password");
        row.put("role", "MEMBER");
        resultData.add(row);

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq("SELECT * FROM users"))).thenReturn(resultData);

            // Call the method under test
            List<User> users = channelService.getAllUsers();

            // Verify the results
            assertEquals(1, users.size());
            assertEquals("testUser", users.get(0).getUsername());
            assertEquals("password", users.get(0).getPassword());
            assertEquals(User.UserRole.MEMBER, users.get(0).getRole());
        }
    }

    @Test
    public void testFindUser() {
        // Prepare test data
        List<Map<String, Object>> resultData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("username", "testUser");
        row.put("role", "MEMBER");
        resultData.add(row);

        String query = """
            SELECT u.username, u.role
            FROM users u
            WHERE LOWER(username) LIKE LOWER(?)
            """;

        // Mock the static executeQuery method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(query), eq("%test%"))).thenReturn(resultData);

            // Call the method under test
            List<User> users = channelService.findUser("test");

            // Verify the results
            assertEquals(1, users.size());
            assertEquals("testUser", users.get(0).getUsername());
            assertEquals(User.UserRole.MEMBER, users.get(0).getRole());
        }
    }

    @Test
    public void testGetAdminsCountForChannel() {
        // Create test data
        List<User> users = new ArrayList<>();
        users.add(new User("admin1", null, User.UserRole.ADMIN));
        users.add(new User("admin2", null, User.UserRole.ADMIN));
        users.add(new User("user1", null, User.UserRole.MEMBER));

        // Create a spy of channelService to mock the getUsersInChannel method
        ChannelService spyService = Mockito.spy(channelService);
        doReturn(users).when(spyService).getUsersInChannel("TestChannel");

        // Call the method under test
        int adminsCount = spyService.getAdminsCountForChannel("TestChannel");

        // Verify the results
        assertEquals(2, adminsCount);
    }

    @Test
    public void testSendMessage() {
        // Prepare parameters
        String channelName = "General";
        String content = "Test message";
        String sender = "testUser";

        String query = "INSERT INTO messages (text, username, channel_name, date_time) VALUES (?, ?, ?, ?)";

        // Mock the static executeUpdate method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(query), eq("Error sending message to channel"), eq(content), eq(sender), eq(channelName), any(Timestamp.class))).thenReturn(1);

            // Call the method under test
            channelService.sendMessage(channelName, content, sender);

            // Verify the method was called
            mockedStatic.verify(() -> ServiceUtility.executeUpdate(eq(query), eq("Error sending message to channel"), eq(content), eq(sender), eq(channelName), any(Timestamp.class)));
        }
    }

    @Test
    public void testCreateChannel_Success() {
        // Prepare parameters
        String channelName = "NewChannel";
        String creatorUsername = "testUser";

        String createChannelQuery = "INSERT INTO channels (name, type) VALUES (?, 'PC')";
        String addUserToChannelQuery = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";

        // Mock the static executeUpdate method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(createChannelQuery), eq("Error inserting channel"), eq(channelName))).thenReturn(1);

            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(addUserToChannelQuery), eq("Error adding user to channel"), eq(creatorUsername), eq(channelName))).thenReturn(1);

            // Call the method under test
            int result = channelService.createChannel(channelName, creatorUsername);

            // Verify the result
            assertEquals(1, result);
        }
    }

    @Test
    public void testCreateChannel_NoUserLoggedIn() {
        // Prepare parameters
        String channelName = "NewChannel";
        String creatorUsername = null;

        // Call the method under test
        int result = channelService.createChannel(channelName, creatorUsername);

        // Verify the result
        assertEquals(-1, result);
    }

    @Test
    public void testCreateChannel_FailToCreateChannel() {
        // Prepare parameters
        String channelName = "NewChannel";
        String creatorUsername = "testUser";

        String createChannelQuery = "INSERT INTO channels (name, type) VALUES (?, 'PC')";

        // Mock the static executeUpdate method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(createChannelQuery), eq("Error inserting channel"), eq(channelName))).thenReturn(0);

            // Call the method under test
            int result = channelService.createChannel(channelName, creatorUsername);

            // Verify the result
            assertEquals(-1, result);
        }
    }

    @Test
    public void testDeleteChannel_Success() {
        // Prepare parameters
        String channelName = "ChannelToDelete";

        String checkChannelQuery = "SELECT COUNT(*) AS count FROM channels WHERE name = ?";
        String deleteUserChannelQuery = "DELETE FROM user_channel WHERE channel_name = ?";
        String deleteChannelQuery = "DELETE FROM channels WHERE name = ?";

        // Prepare mock result
        List<Map<String, Object>> checkResult = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("count", 1L);
        checkResult.add(row);

        // Mock the static methods
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(checkChannelQuery), eq(channelName))).thenReturn(checkResult);

            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(deleteUserChannelQuery), eq("Error deleting from user_channel"), eq(channelName))).thenReturn(1);

            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(deleteChannelQuery), eq("Error deleting channel"), eq(channelName))).thenReturn(1);

            // Call the method under test
            int result = channelService.deleteChannel(channelName);

            // Verify the result
            assertEquals(1, result);
        }
    }

    @Test
    public void testDeleteChannel_ChannelDoesNotExist() {
        // Prepare parameters
        String channelName = "NonExistentChannel";

        String checkChannelQuery = "SELECT COUNT(*) AS count FROM channels WHERE name = ?";

        // Prepare mock result
        List<Map<String, Object>> checkResult = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("count", 0L);
        checkResult.add(row);

        // Mock the static methods
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeQuery(eq(checkChannelQuery), eq(channelName))).thenReturn(checkResult);

            // Call the method under test
            int result = channelService.deleteChannel(channelName);

            // Verify the result
            assertEquals(-1, result);
        }
    }

    @Test
    public void testJoinChannel_Success() {
        // Prepare parameters
        String channelName = "ChannelToJoin";
        String username = "testUser";

        String sql = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";

        // Mock the static executeUpdate method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(sql), eq("Error joining the server"), eq(username), eq(channelName))).thenReturn(1);

            // Call the method under test
            boolean result = channelService.joinChannel(channelName, username);

            // Verify the result
            assertTrue(result);
        }
    }

    @Test
    public void testJoinChannel_Failure() {
        // Prepare parameters
        String channelName = "ChannelToJoin";
        String username = "testUser";

        String sql = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";

        // Mock the static executeUpdate method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(sql), eq("Error joining the server"), eq(username), eq(channelName))).thenReturn(0);

            // Call the method under test
            boolean result = channelService.joinChannel(channelName, username);

            // Verify the result
            assertFalse(result);
        }
    }

    @Test
    public void testCreateDMChannel_Success() {
        // Prepare parameters
        String channelName = "DM_Channel";
        String user1 = "user1";
        String user2 = "user2";

        String createChannelQuery = "INSERT INTO channels (name, type) VALUES (?, 'DM')";
        String addUserToChannelQuery = "INSERT INTO user_channel (username, channel_name) VALUES (?, ?)";

        // Mock the static executeUpdate method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(createChannelQuery), eq("Error inserting channel"), eq(channelName))).thenReturn(1);

            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(addUserToChannelQuery), eq("Error adding user to channel"), eq(user1), eq(channelName))).thenReturn(1);

            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(addUserToChannelQuery), eq("Error adding user to channel"), eq(user2), eq(channelName))).thenReturn(1);

            // Call the method under test
            int result = channelService.createDMChannel(channelName, user1, user2);

            // Verify the result
            assertEquals(1, result);
        }
    }

    @Test
    public void testCreateDMChannel_NullUser1() {
        // Prepare parameters
        String channelName = "DM_Channel";
        String user1 = null;
        String user2 = "user2";

        // Call the method under test
        int result = channelService.createDMChannel(channelName, user1, user2);

        // Verify the result
        assertEquals(-1, result);
    }

    @Test
    public void testCreateDMChannel_NullUser2() {
        // Prepare parameters
        String channelName = "DM_Channel";
        String user1 = "user1";
        String user2 = null;

        // Call the method under test
        int result = channelService.createDMChannel(channelName, user1, user2);

        // Verify the result
        assertEquals(-1, result);
    }

    @Test
    public void testCreateDMChannel_FailToCreateChannel() {
        // Prepare parameters
        String channelName = "DM_Channel";
        String user1 = "user1";
        String user2 = "user2";

        String createChannelQuery = "INSERT INTO channels (name, type) VALUES (?, 'DM')";

        // Mock the static executeUpdate method
        try (MockedStatic<ServiceUtility> mockedStatic = Mockito.mockStatic(ServiceUtility.class)) {
            mockedStatic.when(() -> ServiceUtility.executeUpdate(eq(createChannelQuery), eq("Error inserting channel"), eq(channelName))).thenReturn(0);

            // Call the method under test
            int result = channelService.createDMChannel(channelName, user1, user2);

            // Verify the result
            assertEquals(-1, result);
        }
    }
}