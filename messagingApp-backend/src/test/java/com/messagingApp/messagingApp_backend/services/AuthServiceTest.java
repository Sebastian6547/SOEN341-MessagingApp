package com.messagingApp.messagingApp_backend.services;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {


    private AuthService authService;

    @Mock
    private HttpSession mockSession;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private  Statement mockStatement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService();

        // Create mock instance to be used during the test
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockSession = mock(HttpSession.class);
    }

    @AfterEach
    void tearDown() {
        // Reset every mock for the next test
        Mockito.reset(mockConnection, mockStatement, mockPreparedStatement, mockResultSet, mockSession);
    }

    @Test
    void authenticateUserSuccess() throws SQLException {
        String username = "testUser";   // sample input username
        String password = "testPass";   // Sample input password
        String storedPassword = "testPass";  // sample stored password in database

        // Define mock getConnection()
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            // Define mock database connection
            when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);

            // Mock Statement for "SET ROLE postgres;"
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.execute("SET ROLE postgres;")).thenReturn(true);

            // Define what happen when the function interact with database
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("password")).thenReturn(storedPassword);

            // Define the mock session to have no loggedInUser yet
            when(mockSession.getAttribute("loggedInUser")).thenReturn(null);

            // Call the actual method
            boolean result = authService.authenticateUser(username, password, mockSession);

            // Assertions
            assertTrue(result, "Authentication should be successful");
            verify(mockSession).setAttribute("loggedInUser", username); // Verify session is set
            verify(mockStatement).execute("SET ROLE postgres;"); // Ensure role setting is executed
        }
    }

    @Test
    void authenticateUserFailPassword() throws SQLException {
        String username = "testUser";   // sample input username
        String password = "testPass";   // Sample input password
        String storedPassword = "testFail";  // sample stored password in database

        // Define mock DriverManager
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            // Define mock database connection
            when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);

            // Mock Statement for "SET ROLE postgres;"
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.execute("SET ROLE postgres;")).thenReturn(true);

            // Define what happen when the function interact with database
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("password")).thenReturn(storedPassword);

            // Define the mock session to have no loggedInUser yet
            when(mockSession.getAttribute("loggedInUser")).thenReturn(null);

            // Call the actual method
            boolean result = authService.authenticateUser(username, password, mockSession);

            // Assertions
            assertFalse(result, "Authentication should be fail");
            verify(mockSession, never()).setAttribute(anyString(), any()); // Verify that the session is not interacted to change the loggedInUser
            verify(mockStatement).execute("SET ROLE postgres;"); // Ensure role setting is executed
        }
    }

    @Test
    void authenticateUserFailUsername() throws SQLException {
        String username = "testUser";   // sample input username
        String password = "testPass";   // Sample input password

        // Define mock DriverManager
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            // Define mock database connection
            when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);

            // Mock Statement for "SET ROLE postgres;"
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.execute("SET ROLE postgres;")).thenReturn(true);

            // Define what happen when the function interact with database
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);


            // Define the mock session to have no loggedInUser yet
            when(mockSession.getAttribute("loggedInUser")).thenReturn(null);

            // Call the actual method
            boolean result = authService.authenticateUser(username, password, mockSession);

            // Assertions
            assertFalse(result, "Authentication should be fail");
            verify(mockSession, never()).setAttribute(anyString(), any()); // Verify that the session is not interacted to change the loggedInUser
            verify(mockStatement).execute("SET ROLE postgres;"); // Ensure role setting is executed
        }
    }

    @Test
    void authenticateUserFailUsernameAndPassword() throws SQLException {
        String username = "testUser";   // sample input username
        String password = "testPass";   // Sample input password
        String storedPassword = "testFail";  // sample stored password in database

        // Define mock DriverManager
        try (MockedStatic<DriverManager> ignored = mockStatic(DriverManager.class)) {
            // Define mock database connection
            when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);

            // Mock Statement for "SET ROLE postgres;"
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.execute("SET ROLE postgres;")).thenReturn(true);

            // Define what happen when the function interact with database
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);
            when(mockResultSet.getString("password")).thenReturn(storedPassword);

            // Define the mock session to have no loggedInUser yet
            when(mockSession.getAttribute("loggedInUser")).thenReturn(null);

            // Call the actual method
            boolean result = authService.authenticateUser(username, password, mockSession);

            // Assertions
            assertFalse(result, "Authentication should be fail");
            verify(mockSession, never()).setAttribute(anyString(), any()); // Verify that the session is not interacted to change the loggedInUser
            verify(mockStatement).execute("SET ROLE postgres;"); // Ensure role setting is executed
        }
    }

    @Test
    void getLoggedInUserWhenLoggedIn() {
        String username = "testUser";   // sample input username
        when(mockSession.getAttribute("loggedInUser")).thenReturn(username);
        assertEquals(username, authService.getLoggedInUser(mockSession));
    }

    @Test
    void getLoggedInUserWhenLoggedOut() {
        String username = "testUser";   // sample input username
        when(mockSession.getAttribute("loggedInUser")).thenReturn(null);
        assertNotEquals(username, authService.getLoggedInUser(mockSession));
    }

    @Test
    void logoutWhenLoggedIn() throws SQLException {
        String username = "testUser";   // sample input username
        when(mockSession.getAttribute("loggedInUser")).thenReturn(username);
        authService.logout(mockSession);

        verify(mockSession).invalidate(); // invalidate is called
    }
    @Test
    void logoutWhenLoggedOut() throws SQLException {
        authService.logout(mockSession);
        verify(mockSession).invalidate(); // Invalidate is still called
    }
}