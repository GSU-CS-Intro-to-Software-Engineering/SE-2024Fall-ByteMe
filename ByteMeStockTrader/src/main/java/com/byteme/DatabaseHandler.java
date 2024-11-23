package com.byteme;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.InputStream;
import java.math.BigDecimal;

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
    private void tryCreateStockTable(String stockSymbol) {
        String tableName = "stock_data_" + stockSymbol.toLowerCase();
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "date TIMESTAMP PRIMARY KEY, " +
                "num_positive_articles INT, " +
                "num_neutral_articles INT, " +
                "num_negative_articles INT, " +
                "open_price DECIMAL(10, 2), " +
                "previous_close_price DECIMAL(10, 2), " +
                "high_price DECIMAL(10, 2), " +
                "low_price DECIMAL(10, 2), " +
                "close_price DECIMAL(10, 2), " +
                "volume BIGINT, " +
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert or update data in the specific stock's table
    public void insertOrUpdateStockData(Timestamp timestamp, String stockSymbol, int positiveCount, int neutralCount,
            int negativeCount, Map<String, Object> indicatorData) {
        tryCreateStockTable(stockSymbol); // Ensure the table exists

        String tableName = "stock_data_" + stockSymbol.toLowerCase();
        StringBuilder columns = new StringBuilder(
                "date, num_positive_articles, num_neutral_articles, num_negative_articles, " +
                        "open_price, previous_close_price, high_price, low_price, close_price, volume");
        StringBuilder placeholders = new StringBuilder("?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
        StringBuilder updates = new StringBuilder(
                "num_positive_articles=VALUES(num_positive_articles), " +
                        "num_neutral_articles=VALUES(num_neutral_articles), " +
                        "num_negative_articles=VALUES(num_negative_articles), " +
                        "open_price=VALUES(open_price), previous_close_price=VALUES(previous_close_price), " +
                        "high_price=VALUES(high_price), low_price=VALUES(low_price), " +
                        "close_price=VALUES(close_price), volume=VALUES(volume)");

        // Append other indicators to the SQL query, excluding explicitly handled ones
        for (Map.Entry<String, Object> entry : indicatorData.entrySet()) {
            String key = entry.getKey();

            // Normalize keys to prevent duplicates
            if (key.equals("high"))
                key = "high_price";
            if (key.equals("low"))
                key = "low_price";
            if (key.equals("close"))
                key = "close_price";

            // Skip keys that are already explicitly handled
            if (!key.equals("open_price") && !key.equals("previous_close_price") &&
                    !key.equals("high_price") && !key.equals("low_price") &&
                    !key.equals("close_price") && !key.equals("volume")) {
                columns.append(", ").append(key);
                placeholders.append(", ?");
                updates.append(", ").append(key).append("=VALUES(").append(key).append(")");
            }
        }

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders
                + ") ON DUPLICATE KEY UPDATE " + updates + ";";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set explicit parameters
            pstmt.setTimestamp(1, timestamp);
            pstmt.setInt(2, positiveCount);
            pstmt.setInt(3, neutralCount);
            pstmt.setInt(4, negativeCount);
            pstmt.setObject(5, indicatorData.get("open_price"));
            pstmt.setObject(6, indicatorData.get("previous_close_price"));
            pstmt.setObject(7, indicatorData.get("high"));
            pstmt.setObject(8, indicatorData.get("low"));
            pstmt.setObject(9, indicatorData.get("close_price"));
            pstmt.setObject(10, indicatorData.get("volume"));

            // Set dynamic indicator values
            int index = 11; // Start from the next index after explicit parameters
            for (Map.Entry<String, Object> entry : indicatorData.entrySet()) {
                String key = entry.getKey();

                // Normalize keys and exclude explicitly handled ones
                if (key.equals("high"))
                    key = "high_price";
                if (key.equals("low"))
                    key = "low_price";
                if (key.equals("close"))
                    key = "close_price";

                if (!key.equals("open_price") && !key.equals("previous_close_price") &&
                        !key.equals("high_price") && !key.equals("low_price") &&
                        !key.equals("close_price") && !key.equals("volume")) {
                    pstmt.setObject(index++, entry.getValue());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Data successfully inserted/updated in table '" + tableName + "'.");
        } catch (SQLException e) {
            System.out.println("BRUHHH '" + e.getMessage() + "'");
            e.printStackTrace();
        }
    }

    private void tryCreateTradeTable(String stockSymbol) {
        String tableName = "trade_data_" + stockSymbol.toLowerCase();
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "date TIMESTAMP PRIMARY KEY, " +
                "action VARCHAR(10), " + // e.g., "buy", "sell", "hold"
                "quantity INT, " +
                "price DECIMAL(10, 2), " + // Price of the stock
                "info TEXT" + // Descriptive trade details
                ");";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("Trade table '" + tableName + "' created or already exists.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert or update data in the specific stock's table
    public void insertOrUpdateTradeData(Timestamp timestamp, String stockSymbol, String action, String quantity,
            String price,
            String info) {
        tryCreateTradeTable(stockSymbol); // Ensure the trade table exists

        String tableName = "trade_data_" + stockSymbol.toLowerCase();
        String sql = "INSERT INTO " + tableName + " (date, action, quantity, price, info) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "action = VALUES(action), " +
                "quantity = VALUES(quantity), " +
                "price = VALUES(price), " + // Fixed this line
                "info = VALUES(info);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, timestamp);
            pstmt.setString(2, action);
            pstmt.setInt(3, Integer.parseInt(quantity));
            pstmt.setBigDecimal(4, new BigDecimal(price)); // Convert price to BigDecimal
            pstmt.setString(5, info);

            pstmt.executeUpdate();
            System.out.println("Trade data successfully inserted/updated in table '" + tableName + "'.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch the latest row of data from the stock's table
    public Map<String, Object> getLastRowOfStockData(String stockSymbol) {
        String tableName = "stock_data_" + stockSymbol.toLowerCase();
        String sql = "SELECT * FROM " + tableName + " ORDER BY date DESC LIMIT 1;";
        Map<String, Object> latestRowData = new HashMap<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                latestRowData.put("date", rs.getTimestamp("date"));
                latestRowData.put("num_positive_articles", rs.getInt("num_positive_articles"));
                latestRowData.put("num_neutral_articles", rs.getInt("num_neutral_articles"));
                latestRowData.put("num_negative_articles", rs.getInt("num_negative_articles"));
                latestRowData.put("open_price", rs.getDouble("open_price"));
                latestRowData.put("previous_close_price", rs.getDouble("previous_close_price"));
                latestRowData.put("high_price", rs.getDouble("high_price"));
                latestRowData.put("low_price", rs.getDouble("low_price"));
                latestRowData.put("close_price", rs.getDouble("close_price"));
                latestRowData.put("volume", rs.getLong("volume"));
                latestRowData.put("sma", rs.getDouble("sma"));
                latestRowData.put("ema", rs.getDouble("ema"));
                latestRowData.put("rsi", rs.getDouble("rsi"));
                latestRowData.put("macd", rs.getDouble("macd"));
                latestRowData.put("macd_signal", rs.getDouble("macd_signal"));
                latestRowData.put("macd_hist", rs.getDouble("macd_hist"));
                latestRowData.put("upper_band", rs.getDouble("upper_band"));
                latestRowData.put("middle_band", rs.getDouble("middle_band"));
                latestRowData.put("lower_band", rs.getDouble("lower_band"));
                latestRowData.put("obv", rs.getLong("obv"));
                latestRowData.put("atr", rs.getDouble("atr"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching the last row of data: " + e.getMessage());
            e.printStackTrace();
        }

        return latestRowData;
    }

    public List<String> getLast10RowsOfTradeData(String stockSymbol) {
        List<String> tradeInfoList = new ArrayList<>();
        String tableName = "trade_data_" + stockSymbol.toLowerCase();
        String sql = "SELECT info FROM " + tableName + " ORDER BY date DESC LIMIT 10;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tradeInfoList.add(rs.getString("info")); // Add the `info` column value to the list
            }
        } catch (SQLException e) {
            System.err.println("Error fetching last 10 rows of trade data for " + stockSymbol + ": " + e.getMessage());
        }

        return tradeInfoList;
    }

    // Close the database connection done
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
