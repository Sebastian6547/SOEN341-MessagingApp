package com.messagingApp.messagingApp_backend.controllers;

import com.messagingApp.messagingApp_backend.models.Channel;
import com.messagingApp.messagingApp_backend.models.Message;
import com.messagingApp.messagingApp_backend.models.User;
import com.messagingApp.messagingApp_backend.services.AuthService;
import com.messagingApp.messagingApp_backend.services.ChannelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channel")
public class ChannelController {
    private final ChannelService channelService;
    private final AuthService authService;

    public ChannelController(ChannelService channelService, AuthService authService) {
        this.channelService = channelService;
        this.authService = authService;
    }


    // Get all data for a channel
    @GetMapping("/{channelName}")
    public ResponseEntity<?> getChannelData(@PathVariable String channelName, HttpSession session) {
        //Check user is logged in
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }
        System.out.println("REQUEST by User: " + username + " for channel: " + channelName);

        //Check channel exists
        List<Channel> channels = channelService.getAllChannels();
        boolean isExist = channels.stream().anyMatch(channel -> channel.getName().equals(channelName));
        if (!isExist) {
            return ResponseEntity.status(404).body(Map.of("error", "Channel does not exist"));
        }

        //Check if the user is a member of the channel
        List<Channel> userChannels = channelService.getUserChannels(username);
        boolean isMember = userChannels.stream().anyMatch(channel -> channel.getName().equals(channelName));
        if (!isMember) {
            return ResponseEntity.status(403).body(Map.of("error", "User is not a member of the channel"));
        }

        //Get channel data
        List<User> users = channelService.getUsersInChannel(channelName);
        List<Message> messages = channelService.getMessagesInChannel(channelName);
        Long lastMessageID = channelService.getLastSeenMsg(username, channelName);

        return ResponseEntity.ok(Map.of("channels", userChannels, // All channels the user is in
            "users", users, // Users in the selected channel
            "messages", messages, // Messages in the selected channel
            "lastMessageID", lastMessageID //Last seen message by loggedin User
        ));
    }

    // Send a message to a channel
    @PostMapping("/{channelName}/sendMessage")
    public ResponseEntity<?> sendMessage(@PathVariable String channelName, @RequestBody Map<String, String> messageData, HttpSession session) {
        //Check user is logged in
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        //Check if the user is a member of the channel
        List<Channel> userChannels = channelService.getUserChannels(username);
        boolean isMember = userChannels.stream().anyMatch(channel -> channel.getName().equals(channelName));
        if (!isMember) {
            return ResponseEntity.status(403).body(Map.of("error", "User is not a member of the channel"));
        }

        //Send message
        channelService.sendMessage(channelName, messageData.get("content"), username);
        // Simulate storing message (in real case, insert into DB)
        System.out.println("Message sent to " + channelName + ": " + messageData.get("content"));

        return ResponseEntity.ok(Map.of("message", "Message sent successfully"));
    }

    // Get the latest message in a channel
    @GetMapping("/{channelName}/latest")
    public ResponseEntity<?> getLatestMessage(@PathVariable String channelName, HttpSession session) {
        //Check user is logged in
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        //Check if the user is a member of the channel
        List<Channel> userChannels = channelService.getUserChannels(username);
        boolean isMember = userChannels.stream().anyMatch(channel -> channel.getName().equals(channelName));
        if (!isMember) {
            return ResponseEntity.status(403).body(Map.of("error", "User is not a member of the channel"));
        }

        //Get latest message
        Message latestMessage = channelService.getLatestMessageInChannel(channelName);

        // If there are no messages in the channel
        if (latestMessage == null) {
            return ResponseEntity.ok(Map.of("message", "No messages yet"));
        }

        return ResponseEntity.ok(latestMessage);
    }

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        //Check user is logged in
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        //Get all users
        List<User> users = channelService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get the number of admins in a specific channel
    @GetMapping("/admins-count/{channelName}")
    public ResponseEntity<Map<String, Integer>> getAdminsCountForChannel(@PathVariable String channelName) {
        int adminsCount = channelService.getAdminsCountForChannel(channelName);
        Map<String, Integer> response = new HashMap<>();
        response.put("adminsCount", adminsCount);
        return ResponseEntity.ok(response);
    }

    // Creating a channel
    @PostMapping("/create-channel")
    public ResponseEntity<?> createChannel(@RequestBody Map<String, String> channelData, HttpSession session) {
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        String channelName = channelData.get("formattedChannelName");
        String creatorUsername = channelData.get("loggedUser");

        channelService.createChannel(channelName, creatorUsername);

        System.out.println("Channel created: " + channelName + " by " + creatorUsername);

        return ResponseEntity.ok(Map.of("message", "Channel created successfully"));
    }

    // Deleting a channel
    @DeleteMapping("/delete-channel/{channelName}")
    public ResponseEntity<String> deleteChannel(@PathVariable String channelName) {
        int result = channelService.deleteChannel(channelName);

        if (result > 0) {
            return ResponseEntity.ok("Channel deleted successfully: " + channelName);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to delete channel.");
        }
    }

    // Joining a channel
    @PostMapping("/join")
    public ResponseEntity<?> joinChannel(@RequestBody Map<String, String> channelData, HttpSession session) {
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        String channelName = channelData.get("formattedChannelName");

        System.out.println("Channel joined successfully: " + channelName + " by " + username);
        boolean result = channelService.joinChannel(channelName, username);
        if (result) {
            return ResponseEntity.ok("Channel joined successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to join channel.");
        }
    }

    // Get users from a search input
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(HttpSession session, @RequestParam String query) {
        //check user logged in
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }
        //get users based off search
        List<User> matchUsers = channelService.findUser(query);

        if (matchUsers.size() == 0) {
            return ResponseEntity.status(404).body(Map.of("error", "no matching users found"));
        }
        return ResponseEntity.ok(matchUsers);
    }

    // Update the last seen messages
    @PostMapping("/{channelName}/updateLastSeenMessage")
    public ResponseEntity<String> updateLastSeenMessage(@PathVariable String channelName, HttpSession session, @RequestBody Map<String, Object> messageData) {
        try {
            String username = authService.getLoggedInUser(session);
            Long lastSeenMessageID = ((Number) messageData.get("lastSeenMessageID")).longValue();

            channelService.updateMessageSeenTable(username, channelName, lastSeenMessageID);
            return ResponseEntity.ok("Last seen message updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating last seen message: " + e.getMessage());
        }
    }

    // Get channels with unread messages
    @GetMapping("/getUnreadChannels")
    public ResponseEntity<List<String>> getUnreadChannels(HttpSession session) {
        try {
            String username = authService.getLoggedInUser(session);
            List<String> unreadChannels = channelService.getUnreadChannels(username);
            return ResponseEntity.ok(unreadChannels);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    //Create a channel for DM between 2 users.
    @PostMapping("/create-dm-channel")
    public ResponseEntity<?> createDMChannel(@RequestBody Map<String, String> channelData, HttpSession session) {
        String username = authService.getLoggedInUser(session);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }
        String user1 = channelData.get("user1");
        String user2 = channelData.get("user2");
        String channelName = channelData.get("channelName");

        channelService.createDMChannel(channelName, user1, user2);

        System.out.println("Channel created: " + channelName + " with users " + user1 + " and " + user2);

        return ResponseEntity.ok(Map.of("message", "Channel created successfully"));
    }
}
