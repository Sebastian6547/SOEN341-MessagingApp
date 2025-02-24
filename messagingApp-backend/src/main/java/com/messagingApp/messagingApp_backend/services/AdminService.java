package com.messagingApp.messagingApp_backend.services;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.sql.*;


@Service
public class AdminService {

    private final AuthService authService;
    // Load .env variables
    private static final Dotenv dotenv = Dotenv.load();

    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String DB_USER = dotenv.get("DB_USER");
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    public AdminService(AuthService authService) {
        this.authService = authService;
    }

    public boolean deleteMessage(Long msgId,  HttpSession session) {
        System.out.println("Received message delete request for id " + msgId);
        // Debugging database credentials
        //System.out.println("Database URL: " + DB_URL);
        //System.out.println("Database User: " + DB_USER);
        //System.out.println("Database Password: " + (DB_PASSWORD != null ? "Set" : "Not Set"));

        // Double check to see if the user is an admin
        if (!isAdmin(session)){
            return false;
        }

        // Check if credentials are loaded
        if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
            System.out.println("Error: Database credentials are not set.");
            return false;
        }
        String sql = "DELETE FROM messages WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Set the role to postgres
            statement.execute("SET ROLE postgres;");

            //Executing the statement
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, msgId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Message deleted successfully.");
                    return true;
                } else {
                    System.out.println("No message found with ID: " + msgId);
                    return false;
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error during deletion:");
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAdmin(HttpSession session) {
        System.out.println("Received message isAdmin request");

        // Debugging database credentials
        //System.out.println("Database URL: " + DB_URL);
        //System.out.println("Database User: " + DB_USER);
        //System.out.println("Database Password: " + (DB_PASSWORD != null ? "Set" : "Not Set"));

        // Check if credentials are loaded
        if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
            System.out.println("Error: Database credentials are not set.");
            return false;
        }

        // Retrieving the username from the session
        String username = authService.getLoggedInUser(session);
        String sql = "SELECT role FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Set the role to postgres
            statement.execute("SET ROLE postgres;");

            // Retrieving the result
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                ResultSet resultSet = ps.executeQuery();

                if (resultSet.next()) {
                    String storedRole = resultSet.getString("role");

                    // Check if the returned role is "ADMIN"
                    if (storedRole.equals("ADMIN")) {
                        System.out.println("User "+username+" has admin role");
                        return true;
                    } else {
                        System.out.println("User "+username+" doesn't have admin role");
                        return false;
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
}
