package com.byteme;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseHandler {

    private static final String DB_PROPERTIES_FILE = "/db.properties";
    private Connection connection;

    // Constructor that initializes the database connection
    public DatabaseHandler() {
        initializeConnection();
    }

    // Method to initialize the database connection using db.properties
    private void initializeConnection() {
        Properties props = new Properties();
        try (InputStream input = DatabaseHandler.class.getResourceAsStream(DB_PROPERTIES_FILE)) {
            props.load(input);
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            connection = DriverManager.getConnection(url, username, password);
            System.out.println("\nConnected to the MySQL database!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create a table for the specific stock symbol if it doesn't exist
    private void createTableIfNotExists(String stockSymbol) {
        String tableName = "stock_data_" + stockSymbol.toLowerCase();
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "date TIMESTAMP PRIMARY KEY, " +
                "num_positive_articles INT, " +
                "num_neutral_articles INT, " +
                "num_negative_articles INT, " +
                "avg_open DECIMAL(10, 2), " +
                "avg_high DECIMAL(10, 2), " +
                "avg_low DECIMAL(10, 2), " +
                "avg_close DECIMAL(10, 2), " +
                "avg_volume BIGINT" +
                ");";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("Table '" + tableName + "' initialized or already exists.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert or update data in the specific stock's table
    public void insertOrUpdateData(Timestamp timestamp, String stockSymbol, int positiveCount, int neutralCount,
            int negativeCount, double avgOpen, double avgHigh, double avgLow,
            double avgClose, long avgVolume) {
        createTableIfNotExists(stockSymbol); // Ensure the table exists

        String tableName = "stock_data_" + stockSymbol.toLowerCase();
        String sql = "INSERT INTO " + tableName
                + " (date, num_positive_articles, num_neutral_articles, num_negative_articles, " +
                "avg_open, avg_high, avg_low, avg_close, avg_volume) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE num_positive_articles=VALUES(num_positive_articles), " +
                "num_neutral_articles=VALUES(num_neutral_articles), num_negative_articles=VALUES(num_negative_articles), "
                +
                "avg_open=VALUES(avg_open), avg_high=VALUES(avg_high), avg_low=VALUES(avg_low), " +
                "avg_close=VALUES(avg_close), avg_volume=VALUES(avg_volume);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, timestamp);
            pstmt.setInt(2, positiveCount);
            pstmt.setInt(3, neutralCount);
            pstmt.setInt(4, negativeCount);
            pstmt.setDouble(5, avgOpen);
            pstmt.setDouble(6, avgHigh);
            pstmt.setDouble(7, avgLow);
            pstmt.setDouble(8, avgClose);
            pstmt.setLong(9, avgVolume);

            pstmt.executeUpdate();
            System.out.println("Data successfully inserted/updated in table '" + tableName + "'.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Close the database connection when done
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
