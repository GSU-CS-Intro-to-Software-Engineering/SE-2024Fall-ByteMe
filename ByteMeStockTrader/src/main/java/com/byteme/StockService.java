package com.byteme;

import com.byteme.DataRetreival.NumericalDataFetcher;
import com.byteme.DataRetreival.StockNewsFetcher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {

    private final Map<LocalDate, int[]> dailyNewsSentimentCache = new HashMap<>(); // Cache news sentiment
    private int uploadCounter = 0; // Track uploads since market open
    private boolean isTradingEnabled = true;

    public int getUploadCounter() {
        return this.uploadCounter;
    }

    public void toggleTrader() {
        isTradingEnabled = !isTradingEnabled;
        System.out.println("Trading status updated: " + (isTradingEnabled ? "Enabled" : "Disabled"));
    }

    public boolean isTradingEnabled() {
        return isTradingEnabled;
    }

    @Scheduled(cron = "0 0/1 9-20 * * MON-FRI", zone = "America/New_York")
    public void scheduleStockDataFetch() {
        System.out.println("Scheduled task triggered at " + LocalTime.now());
        if (isTradingEnabled && isMarketOpen()) {
            String symbol = "NVDA";
            System.out.println("Fetching stock data for " + symbol);
            Map<String, Object> result = gatherStockData(symbol);

            // Increment upload counter
            if (Boolean.TRUE.equals(result.get("uploadSuccess"))) {
                uploadCounter++;
                System.out.println("Upload Counter: " + uploadCounter);

                if (uploadCounter >= 10) {
                    analyzeAndTrade(symbol);
                }
            }
        } else {
            System.out.println("Market is closed or trading is disabled. Skipping data fetch.");
        }
    }

    private boolean isMarketOpen() {
        LocalTime now = LocalTime.now(ZoneId.of("America/New_York"));
        return !now.isBefore(LocalTime.of(9, 30)) && !now.isAfter(LocalTime.of(16, 0));
    }

    public Map<String, Object> gatherStockData(String symbol) {
        Map<String, Object> result = new HashMap<>();
        int[] newsSentiment = { 0, 0, 0 };// getNewsSentimentForDay(symbol);
        Map<String, Object> indicatorData = gatherIndicatorData(symbol);

        if (newsSentiment == null || indicatorData == null) {
            result.put("error", "Failed to gather stock data.");
            return result;
        }

        boolean uploadSuccess = uploadDataToDatabase(symbol, newsSentiment, indicatorData);

        // Populate the result
        result.put("uploadSuccess", uploadSuccess);
        result.put("symbol", symbol);
        result.put("positiveSentiment", newsSentiment[0]);
        result.put("neutralSentiment", newsSentiment[1]);
        result.put("negativeSentiment", newsSentiment[2]);

        result.put("open_price", indicatorData.get("open_price"));
        result.put("previous_close_price", indicatorData.get("previous_close_price"));
        result.put("sma", indicatorData.get("sma"));
        result.put("ema", indicatorData.get("ema"));
        result.put("rsi", indicatorData.get("rsi"));
        result.put("macd", indicatorData.get("macd"));
        result.put("macd_signal", indicatorData.get("macd_signal"));
        result.put("macd_hist", indicatorData.get("macd_hist"));
        result.put("upper_band", indicatorData.get("upper_band"));
        result.put("middle_band", indicatorData.get("middle_band"));
        result.put("lower_band", indicatorData.get("lower_band"));
        result.put("obv", indicatorData.get("obv"));
        result.put("atr", indicatorData.get("atr"));

        return result;
    }

    private int[] getNewsSentimentForDay(String symbol) {
        LocalDate today = LocalDate.now(ZoneId.of("America/New_York"));
        if (dailyNewsSentimentCache.containsKey(today)) {
            // Return cached sentiment if already fetched
            return new int[] { 0, 0, 0 };
        } else {
            int[] newsSentiment = gatherNewsSentiment(symbol);
            if (newsSentiment != null) {
                dailyNewsSentimentCache.put(today, newsSentiment); // Cache sentiment for the day
            }
            return newsSentiment;
        }
    }

    public static int[] gatherNewsSentiment(String symbol) {

        try {

            // Instantiate the StockNewsFetcher with selected symbol
            StockNewsFetcher newsFetcher = new StockNewsFetcher(symbol);

            // Fetch stock article titles
            ArrayList<String> titles = newsFetcher.fetchYahooFinanceApiTitles();
            System.out.println("-" + titles.size() + " articles fetched from Yahoo Finance API.\n");

            // Build the command to execute Python script for sentiment analysis
            String workingDir = System.getProperty("user.dir");
            String[] sentimentCommand = { "python3", workingDir + "/src/python/sentiment.py" }; // "/ByteMeStockTrader/src/python/sentiment.py"

            // Create a process to run the Python script
            ProcessBuilder titlesSentimentAnalyzer = new ProcessBuilder(sentimentCommand);
            Process process = titlesSentimentAnalyzer.start();

            System.out.println("-Analyzing article title sentiment using Finbert . . .\n");

            // Write the gathered article titles to Python process as input
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            for (String title : titles) {
                writer.write(title);
                writer.newLine();
            }
            writer.close();

            // Read the Python script output (sentiment scores)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int positiveCount = 0, neutralCount = 0, negativeCount = 0;

            if ((line = reader.readLine()) != null) {
                String[] counts = line.split(",");
                positiveCount = Integer.parseInt(counts[0].trim());
                negativeCount = Integer.parseInt(counts[1].trim());
                neutralCount = Integer.parseInt(counts[2].trim());
            }

            process.waitFor();
            reader.close();

            return new int[] { positiveCount, neutralCount, negativeCount };

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Object> gatherIndicatorData(String symbol) {
        try {
            NumericalDataFetcher dataFetcher = new NumericalDataFetcher(symbol);
            System.out.println("-Fetching current indicators for " + symbol + "...");

            JsonObject indicatorJson = dataFetcher.fetchIndicators();

            if (indicatorJson == null || indicatorJson.size() == 0) {
                System.out.println("Failed to fetch indicators. Exiting...");
                return null;
            }

            Map<String, Object> indicatorData = new HashMap<>();

            // Handle Open, Close, High, Low, Volume, and Previous Close
            if (indicatorJson.has("OPEN_CLOSE_HIGH_LOW_VOLUME")) {
                JsonObject openCloseHighLowVolume = indicatorJson.getAsJsonObject("OPEN_CLOSE_HIGH_LOW_VOLUME");
                indicatorData.put("open_price", openCloseHighLowVolume.get("open").getAsString());
                indicatorData.put("previous_close_price", openCloseHighLowVolume.get("previous_close").getAsString());
                indicatorData.put("high", openCloseHighLowVolume.get("high").getAsString());
                indicatorData.put("low", openCloseHighLowVolume.get("low").getAsString());
                indicatorData.put("volume", openCloseHighLowVolume.get("volume").getAsString());
                indicatorData.put("close_price", openCloseHighLowVolume.get("close").getAsString());
            }

            // Handle other indicators
            for (Map.Entry<String, JsonElement> entry : indicatorJson.entrySet()) {
                String key = entry.getKey().toLowerCase();
                if (key.equals("open_close_high_low_volume"))
                    continue;

                JsonObject value = entry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> field : value.entrySet()) {
                    indicatorData.put(field.getKey().toLowerCase(), field.getValue().getAsString());
                }
            }

            return indicatorData;

        } catch (Exception e) {
            System.out.println("An error occurred while fetching indicators: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean uploadDataToDatabase(String symbol, int[] newsSentiment, Map<String, Object> indicatorData) {
        try {
            DatabaseHandler dbHandler = new DatabaseHandler();
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

            System.out.println("\nInserting data for " + symbol + " at " + currentTimestamp + "...");
            System.out.println("News sentiment: Positive - " + newsSentiment[0] + ", Neutral - " + newsSentiment[1]
                    + ", Negative - " + newsSentiment[2]);
            System.out.println("Indicators: " + indicatorData);

            // Insert or update the data for the specific stock
            dbHandler.insertOrUpdateData(currentTimestamp, symbol, newsSentiment[0], newsSentiment[1], newsSentiment[2],
                    indicatorData);

            dbHandler.closeConnection();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void analyzeAndTrade(String symbol) {
        System.out.println("Analyzing data and making a trade decision for " + symbol + "...");

        try {
            // Build the command to execute Python script for sentiment analysis
            String workingDir = System.getProperty("user.dir");
            String[] tradeAnalyzerPath = { "python3", workingDir + "/src/python/analyzeAndTrade.py", symbol };

            // Create a process to run the Python script
            ProcessBuilder tradeAnalyzer = new ProcessBuilder(tradeAnalyzerPath);
            Process process = tradeAnalyzer.start();

            // Read the output of the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Python Script Output]: " + line); // Log the script output
            }
            reader.close();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
                uploadCounter = 0;
            } else {
                System.err.println("Python script failed with exit code: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("Error while running Python script: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
