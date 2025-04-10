package com.messagingApp.messagingApp_backend.services;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.*;

public class ServiceUtility {

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

    // Default method to get data from the database
    static List<Map<String, Object>> executeQuery(String query, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement ps = connection.prepareStatement(query)) {
            //Set query parameters
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            //Execute the query
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error during query execution: [" + query + "] with " + Arrays.toString(params));
            e.printStackTrace();
        }
        return result;
    }

    // Default method to update data in the database
    static int executeUpdate(String query, String errorMessage, Object... params) {
        int rowsAffected = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement ps = connection.prepareStatement(query)) {

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
