package com.messagingApp.messagingApp_backend.controllers;

import com.messagingApp.messagingApp_backend.services.ChannelService;
import com.messagingApp.messagingApp_backend.services.AuthService;
import com.messagingApp.messagingApp_backend.models.Channel;
import com.messagingApp.messagingApp_backend.models.Message;
import com.messagingApp.messagingApp_backend.models.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(controllers = ChannelController.class)
@ActiveProfiles("test")
public class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChannelService channelService;

    @MockitoBean
    private AuthService authService;

    @Test
    void getChannelData_ReturnsExpectedData() throws Exception {
        // Setup dummy session username
        MockHttpSession session = new MockHttpSession();
        User mockUser = new User("testUser", "password", User.UserRole.MEMBER);
        Channel mockChannel = new Channel("General", Channel.ChannelType.PC);
        session.setAttribute("username", "testUser");

        // Mock the channelService to return expected data
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getAllChannels()).thenReturn(List.of(mockChannel));
        Mockito.when(channelService.getUserChannels("testUser")).thenReturn(List.of(mockChannel));
        Mockito.when(channelService.getUsersInChannel("General")).thenReturn(List.of(mockUser));
        Mockito.when(channelService.getMessagesInChannel("General")).thenReturn(List.of(
                new Message(1, "Hello", mockUser, mockChannel, LocalDateTime.now())
        ));

        // Perform the request
        mockMvc.perform(get("/api/channel/General").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channels[0].name").value("General"))
                .andExpect(jsonPath("$.users[0].username").value("testUser"))
                .andExpect(jsonPath("$.messages[0].content").value("Hello"));
    }

    @Test
    void getChannelData_WithoutSessionUser_ReturnsUnauthorized() throws Exception {
        // No username in session
        MockHttpSession session = new MockHttpSession();

        // Perform the request without setting "username" in session
        mockMvc.perform(get("/api/channel/General").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getChannelData_UserNotInChannel_ReturnsForbidden() throws Exception {
        // Setup dummy session with logged-in user
        MockHttpSession session = new MockHttpSession();
        Channel mockChannel = new Channel("General", Channel.ChannelType.PC);
        session.setAttribute("username", "testUser");

        // Simulate user not being in the channel
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getAllChannels()).thenReturn(List.of(mockChannel));
        Mockito.when(channelService.getUserChannels("testUser")).thenReturn(List.of()); // No channels returned

        // Perform the request
        mockMvc.perform(get("/api/channel/General").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getChannelData_ChannelNotFound_ReturnsNotFound() throws Exception {
        // Setup dummy session with logged-in user
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        // Simulate channel list that doesn't include "General"
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getAllChannels()).thenReturn(List.of()); // No channels returned
        Mockito.when(channelService.getUserChannels("testUser")).thenReturn(List.of(
                new Channel("General", Channel.ChannelType.PC)
        ));

        // Perform the request
        mockMvc.perform(get("/api/channel/General").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void sendMessage_ValidRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Channel mockChannel = new Channel("General", Channel.ChannelType.PC);
        session.setAttribute("username", "testUser");

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getUserChannels("testUser")).thenReturn(List.of(mockChannel));
        Mockito.doNothing().when(channelService).sendMessage("General", "Hello world", "testUser");

        // JSON request body
        String requestBody =
        """
            {
                "text": "Hello world"
            }
        """;

        mockMvc.perform(post("/api/channel/General/sendMessage")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void sendMessage_UserNotInChannel_ReturnsForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getUserChannels("testUser")).thenReturn(List.of()); // No channels returned
        Mockito.doNothing().when(channelService).sendMessage("General", "Hello world", "testUser");

        // JSON request body
        String requestBody =
                """
                    {
                        "text": "Hello world"
                    }
                """;

        mockMvc.perform(post("/api/channel/General/sendMessage")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLatestMessage_ReturnsLatestMessage() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        User mockUser = new User("testUser", "password", User.UserRole.MEMBER);
        Channel mockChannel = new Channel("General", Channel.ChannelType.PC);
        Message mockMessage = new Message(1, "Latest message", mockUser, mockChannel, LocalDateTime.now());

        // Mock dependencies
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getUserChannels("testUser")).thenReturn(List.of(mockChannel));
        Mockito.when(channelService.getLatestMessageInChannel("General")).thenReturn(mockMessage);

        // Perform the GET request
        mockMvc.perform(get("/api/channel/General/latest").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Latest message"))
                .andExpect(jsonPath("$.sender.username").value("testUser"));
    }

    @Test
    void getLatestMessage_EmptyChannel_Ok() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");
        Channel mockChannel = new Channel("General", Channel.ChannelType.PC);

        // Mock dependencies
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getUserChannels("testUser")).thenReturn(List.of(mockChannel));
        Mockito.when(channelService.getLatestMessageInChannel("General")).thenReturn(null);

        // Perform the GET request
        mockMvc.perform(get("/api/channel/General/latest").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No messages yet"));
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        // Setup session and mock user
        MockHttpSession session = new MockHttpSession();

        User mockUser = new User("testUser", "password", User.UserRole.MEMBER);

        // Mock dependencies
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.getAllUsers()).thenReturn(List.of(mockUser));

        // Perform GET request
        mockMvc.perform(get("/api/channel/users").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testUser"))
                .andExpect(jsonPath("$[0].role").value("MEMBER"));
    }

    @Test
    void getAllUsers_WithoutSessionUser_ReturnsUnauthorized() throws Exception {
        // Setup session and mock user
        MockHttpSession session = new MockHttpSession();

        // Perform GET request
        mockMvc.perform(get("/api/channel/users").session(session).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAdminsCountForChannel_ReturnsCount() throws Exception {

        // Mock dependencies
        Mockito.when(channelService.getAdminsCountForChannel("General")).thenReturn(5);

        // Perform GET request
        mockMvc.perform(get("/api/channel/admins-count/General").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminsCount").value(5));
    }

    @Test
    void createChannel_ValidRequest() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.createChannel("General", "testUser")).thenReturn(1);

        // JSON request body
        String requestBody =
                """
                    {
                        "formattedChannelName": "General",
                        "loggedUser": "testUser"
                    }
                """;

        mockMvc.perform(post("/api/channel/create-channel")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Channel created successfully"));
    }

    @Test
    void deleteChannel_ValidRequest() throws Exception {
        // Mock user session and service call
        Mockito.when(channelService.deleteChannel("General")).thenReturn(1);

        // Perform the DELETE request
        mockMvc.perform(delete("/api/channel/delete-channel/General").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteChannel_ChannelNotFound_ReturnsInternalServerError() throws Exception {
        // Mock user session and service call
        Mockito.when(channelService.deleteChannel("NonExistentChannel")).thenReturn(0);

        // Perform the DELETE request
        mockMvc.perform(delete("/api/channel/delete-channel/NonExistentChannel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void joinChannel_ValidRequest() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.joinChannel("General", "testUser")).thenReturn(true);

        // JSON request body
        String requestBody =
                """
                    {
                        "formattedChannelName": "General"
                    }
                """;

        mockMvc.perform(post("/api/channel/join")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void joinChannel_ChannelNotFound_ReturnsNotFound() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.joinChannel("NonExistentChannel", "testUser")).thenReturn(false);

        // JSON request body
        String requestBody =
                """
                    {
                        "formattedChannelName": "NonExistentChannel"
                    }
                """;

        mockMvc.perform(post("/api/channel/join")
                 .session(session)
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(requestBody))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void searchUsers_ValidRequest() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        User mockUser = new User("testUser", "password", User.UserRole.MEMBER);

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.findUser("test")).thenReturn(List.of(mockUser));

        mockMvc.perform(get("/api/channel/users/search")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testUser"));
    }

    @Test
    void searchUsers_NoUsersFound_ReturnsNotFound() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.findUser("test")).thenReturn(List.of()); // No users found

        mockMvc.perform(get("/api/channel/users/search")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("query", "test"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDMChannel_ValidRequest() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testUser");

        // Mock user session and service call
        Mockito.when(authService.getLoggedInUser(session)).thenReturn("testUser");
        Mockito.when(channelService.createDMChannel("testUser1-testUser2", "testUser1", "testUser2")).thenReturn(1);

        // JSON request body
        String requestBody =
                """
                    {
                        "user1": "testUser1",
                        "user2": "testUser2",
                        "channelName": "testUser1-testUser2"
                    }
                """;

        mockMvc.perform(post("/api/channel/create-dm-channel")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Channel created successfully"));
    }

    @Test
    void createDMChannel_WithoutSessionUser_ReturnsUnauthorized() throws Exception {
        // Setup dummy session and mock data
        MockHttpSession session = new MockHttpSession();

        // JSON request body
        String requestBody =
                """
                    {
                        "user1": "testUser1",
                        "user2": "testUser2",
                        "channelName": "testUser1-testUser2"
                    }
                """;

        mockMvc.perform(post("/api/channel/create-dm-channel")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}
