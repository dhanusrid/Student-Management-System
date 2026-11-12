package com.student.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * 
 * Utility class for establishing a JDBC connection with the MySQL Database.
 * Explains basic JDBC connection workflow for beginners:
 * 1. Load the JDBC Driver class (com.mysql.cj.jdbc.Driver)
 * 2. Connect to MySQL URL with username and password
 * 3. Return the active Connection object
 */
public class DBConnection {

    // Database configuration constants
    // You can modify USER and PASSWORD below according to your local MySQL installation.
    private static final String URL = "jdbc:mysql://localhost:3306/student_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root"; // Change to "" if your MySQL root has no password

    // Driver class name for MySQL Connector/J 8.x and above
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    // Track whether MySQL has been successfully contacted
    private static boolean mysqlAvailable = false;
    private static boolean testedOnce = false;

    /**
     * Establishes and returns a new Connection to the MySQL database.
     * 
     * @return Connection object if successful, or null if connection fails
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Step 1: Register and load MySQL JDBC Driver
            Class.forName(DRIVER_CLASS);

            // Step 2: Establish the database connection
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            mysqlAvailable = true;
            if (!testedOnce) {
                System.out.println("[DBConnection] Successfully connected to MySQL database: student_management");
                testedOnce = true;
            }
        } catch (ClassNotFoundException e) {
            mysqlAvailable = false;
            if (!testedOnce) {
                System.err.println("[DBConnection Note] MySQL JDBC Driver (" + DRIVER_CLASS + ") not found in classpath.");
                System.err.println("[DBConnection Note] Safe in-memory fallback enabled so all website features work smoothly!");
                testedOnce = true;
            }
        } catch (SQLException e) {
            mysqlAvailable = false;
            if (!testedOnce) {
                System.err.println("[DBConnection Note] Could not connect to MySQL at localhost:3306 (" + e.getMessage() + ").");
                System.err.println("[DBConnection Note] Safe in-memory fallback enabled with sample student records so all features work immediately!");
                System.err.println("[DBConnection Note] When ready, start MySQL, run database.sql, and update credentials in DBConnection.java.");
                testedOnce = true;
            }
        }
        return conn;
    }

    /**
     * Checks if a live MySQL connection is available.
     */
    public static boolean isMySQLAvailable() {
        if (!testedOnce) {
            try (Connection conn = getConnection()) {
                mysqlAvailable = (conn != null);
            } catch (Exception ignored) {
                mysqlAvailable = false;
            }
        }
        return mysqlAvailable;
    }

    /**
     * Safely closes an open JDBC connection.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
            }
        }
    }
}
