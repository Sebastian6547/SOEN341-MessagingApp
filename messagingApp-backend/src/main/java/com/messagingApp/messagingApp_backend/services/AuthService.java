package com.messagingApp.messagingApp_backend.services;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

@Service
public class AuthService {

    // Load .env variables
    private static final Dotenv dotenv = Dotenv.load();

    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String DB_USER = dotenv.get("DB_USER");
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    // Check if combination of username and password is valid
    public boolean authenticateUser(String username, String password, HttpSession session) {
        System.out.println("Received login request: " + username + " | " + password);
        System.out.println("Authenticating user: " + username);

        // Debugging database credentials
        System.out.println("Database URL: " + DB_URL);
        System.out.println("Database User: " + DB_USER);
        System.out.println("Database Password: " + (DB_PASSWORD != null ? "Set" : "Not Set"));

        // Check if credentials are loaded
        if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
            System.out.println("Error: Database credentials are not set.");
            return false;
        }

        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Set the role to postgres before executing any other SQL commands
            statement.execute("SET ROLE postgres;");
            System.out.println("Role set to postgres successfully.");

            // Now execute the actual query to authenticate the user
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                ResultSet resultSet = ps.executeQuery();

                if (resultSet.next()) {
                    String storedPassword = resultSet.getString("password");

                    // If the provided password matches the stored one, authenticate
                    if (storedPassword.equals(password)) {
                        session.setAttribute("loggedInUser", username); // Store username in session
                        System.out.println("User " + username + " authenticated successfully.");
                        return true;
                    } else {
                        System.out.println("Invalid password for user: " + username);
                    }
                } else {
                    System.out.println("User not found: " + username);
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error during authentication:");
            e.printStackTrace();
        }

        return false;
    }

    // Get logged-in user
    public String getLoggedInUser(HttpSession session) {
        String user = (String) session.getAttribute("loggedInUser");
        System.out.println("Fetching logged-in user: " + (user != null ? user : "No user logged in"));
        return user;
    }

    // Logout user
    public void logout(HttpSession session) {
        System.out.println("Logging out user: " + session.getAttribute("loggedInUser"));
        session.invalidate();
    }
}
