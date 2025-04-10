package com.messagingApp.messagingApp_backend.controllers;

import com.messagingApp.messagingApp_backend.services.AuthService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_ReturnsSuccessMessage_WhenCredentialsAreValid() throws Exception {
        // Setup dummy session and credentials
        String username = "testUser";
        String password = "testPassword";
        MockHttpSession session = new MockHttpSession();

        // Mock authService
        Mockito.when(authService.authenticateUser(username, password, session)).thenReturn(true);

        String requestContent = """
            {
                "username": "testUser",
                "password": "testPassword"
            }
            """;

        // Perform login request
        mockMvc.perform(post("/api/auth/login").session(session).contentType("application/json").content(requestContent)).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_ReturnsErrorMessage_WhenCredentialsAreInvalid() throws Exception {
        // Setup dummy session and credentials
        String username = "wrongUser";
        String password = "wrongPassword";
        MockHttpSession session = new MockHttpSession();

        // Mock authService
        Mockito.when(authService.authenticateUser(username, password, session)).thenReturn(false);

        String requestContent = """
            {
                "username": "wrongUser",
                "password": "wrongPassword"
            }
            """;

        // Perform login request
        mockMvc.perform(post("/api/auth/login").session(session).contentType(MediaType.APPLICATION_JSON).content(requestContent)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void getLoggedInUser_ReturnsUsername_WhenUserIsLoggedIn() throws Exception {
        // Setup dummy session
        String username = "testUser";
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", username);

        // Mock authService
        Mockito.when(authService.getLoggedInUser(session)).thenReturn(username);

        // Perform check request
        mockMvc.perform(get("/api/auth/check").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void getLoggedInUser_ReturnsError_WhenUserIsNotLoggedIn() throws Exception {
        // Setup dummy session
        MockHttpSession session = new MockHttpSession();

        // Mock authService
        Mockito.when(authService.getLoggedInUser(session)).thenReturn(null);

        // Perform check request
        mockMvc.perform(get("/api/auth/check").session(session)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").value("User not logged in"));
    }

    @Test
    void logout_ReturnsSuccessMessage_WhenUserLogsOut() throws Exception {
        // Setup dummy session
        MockHttpSession session = new MockHttpSession();

        // Mock authService
        Mockito.doNothing().when(authService).logout(session);

        // Perform logout request
        mockMvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void register_ReturnsSuccessMessage_WhenUserIsRegistered() throws Exception {
        // Setup dummy user details
        String username = "newUser";
        String password = "newPassword";
        String role = "user";

        // Mock authService
        Mockito.when(authService.registerUser(username, password, role)).thenReturn(2);

        String requestContent = """
            {
                "username": "newUser",
                "password": "newPassword",
                "role": "user"
            }
            """;

        // Perform registration request
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(requestContent)).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void register_ReturnsErrorMessage_WhenRoleIsInvalid() throws Exception {
        // Setup dummy user details
        String username = "newUser";
        String password = "newPassword";
        String role = "invalidRole";

        // Mock authService
        Mockito.when(authService.registerUser(username, password, role)).thenReturn(1);

        String requestContent = """
            {
                "username": "newUser",
                "password": "newPassword",
                "role": "invalidRole"
            }
            """;

        // Perform registration request
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(requestContent)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid Role"));
    }

    @Test
    void register_ReturnsErrorMessage_WhenUserAlreadyExists() throws Exception {
        // Setup dummy user details
        String username = "existingUser";
        String password = "newPassword";
        String role = "user";

        // Mock authService
        Mockito.when(authService.registerUser(username, password, role)).thenReturn(0);

        String requestContent = """
            {
                "username": "existingUser",
                "password": "newPassword",
                "role": "user"
            }
            """;

        // Perform registration request
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(requestContent)).andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("Error creating user"));
    }
}
