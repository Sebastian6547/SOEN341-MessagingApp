package com.messagingApp.messagingApp_backend.services;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;


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
        if (!isAdmin(session)){
            System.out.println("User is not an admin.");
            return false;
        }
        System.out.println("User is an admin. Deleting this message.");
        String sql = "DELETE FROM messages WHERE id = ?";

        int rowsUpdated = executeUpdate(sql, "Error Deleting Message", msgId);
        if (rowsUpdated > 0) {
            System.out.println("Message deleted successfully.");
            return true;
        } else {
            System.out.println("No message found with ID: " + msgId);
            return false;
        }
    }

    public boolean isAdmin(HttpSession session) {
        System.out.println("Received message isAdmin request");

        // Retrieving the username from the session
        String username = authService.getLoggedInUser(session);
        String sql = "SELECT role FROM users WHERE username = ?";

        List<Map<String,Object>> result = executeQuery(sql, username);
        if (result.get(0)!=null) {
            String storedRole = result.get(0).get("role").toString();

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

        return false;
    }
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
}
