package com.byteme;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
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
                "sma DECIMAL(10, 2), " +
                "ema DECIMAL(10, 2), " +
                "rsi DECIMAL(10, 2), " +
                "macd DECIMAL(10, 2), " +
                "macd_signal DECIMAL(10, 2), " +
                "macd_hist DECIMAL(10, 2), " +
                "upper_band DECIMAL(10, 2), " +
                "middle_band DECIMAL(10, 2), " +
                "lower_band DECIMAL(10, 2), " +
                "obv BIGINT, " +
                "atr DECIMAL(10, 2)" +
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
            int negativeCount, Map<String, Object> indicatorData) {
        createTableIfNotExists(stockSymbol); // Ensure the table exists

        String tableName = "stock_data_" + stockSymbol.toLowerCase();
        StringBuilder columns = new StringBuilder(
                "date, num_positive_articles, num_neutral_articles, num_negative_articles");
        StringBuilder placeholders = new StringBuilder("?, ?, ?, ?");
        StringBuilder updates = new StringBuilder(
                "num_positive_articles=VALUES(num_positive_articles), num_neutral_articles=VALUES(num_neutral_articles), num_negative_articles=VALUES(num_negative_articles)");

        for (String indicator : indicatorData.keySet()) {
            columns.append(", ").append(indicator);
            placeholders.append(", ?");
            updates.append(", ").append(indicator).append("=VALUES(").append(indicator).append(")");
        }

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders
                + ") ON DUPLICATE KEY UPDATE "
                + updates + ";";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, timestamp);
            pstmt.setInt(2, positiveCount);
            pstmt.setInt(3, neutralCount);
            pstmt.setInt(4, negativeCount);

            int index = 5;
            for (Object value : indicatorData.values()) {
                pstmt.setObject(index++, value);
            }

            pstmt.executeUpdate();
            System.out.println("Data successfully inserted/updated in table '" + tableName + "'.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Close the database connection done
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
