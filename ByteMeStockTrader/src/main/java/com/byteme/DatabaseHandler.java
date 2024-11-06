package com.byteme;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
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

    // Method to initialize the database table if it doesn't exist
    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS stock_data (" +
                "date DATE PRIMARY KEY, " +
                "stock_name VARCHAR(10), " +
                "num_positive_articles INT, " +
                "num_neutral_articles INT, " +
                "num_negative_articles INT, " +
                "avg_3week_open DECIMAL(10, 2), " +
                "avg_3week_high DECIMAL(10, 2), " +
                "avg_3week_low DECIMAL(10, 2), " +
                "avg_3week_close DECIMAL(10, 2), " +
                "avg_3week_volume BIGINT" +
                ");";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("Table initialized or already exists.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert or update data in the stock_data table
    public void insertOrUpdateData(Date date, String stockName, int positiveCount, int neutralCount, int negativeCount,
            double avg3WeekOpen, double avg3WeekHigh, double avg3WeekLow,
            double avg3WeekClose, long avg3WeekVolume) {

        String sql = "INSERT INTO stock_data (date, stock_name, num_positive_articles, num_neutral_articles, num_negative_articles, "
                +
                "avg_3week_open, avg_3week_high, avg_3week_low, avg_3week_close, avg_3week_volume) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE stock_name=VALUES(stock_name), num_positive_articles=VALUES(num_positive_articles), "
                +
                "num_neutral_articles=VALUES(num_neutral_articles), num_negative_articles=VALUES(num_negative_articles), "
                +
                "avg_3week_open=VALUES(avg_3week_open), avg_3week_high=VALUES(avg_3week_high), avg_3week_low=VALUES(avg_3week_low), "
                +
                "avg_3week_close=VALUES(avg_3week_close), avg_3week_volume=VALUES(avg_3week_volume);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, date);
            pstmt.setString(2, stockName);
            pstmt.setInt(3, positiveCount);
            pstmt.setInt(4, neutralCount);
            pstmt.setInt(5, negativeCount);
            pstmt.setDouble(6, avg3WeekOpen);
            pstmt.setDouble(7, avg3WeekHigh);
            pstmt.setDouble(8, avg3WeekLow);
            pstmt.setDouble(9, avg3WeekClose);
            pstmt.setLong(10, avg3WeekVolume);

            pstmt.executeUpdate();
            System.out.println("Data successfully inserted/updated in the database.");

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
