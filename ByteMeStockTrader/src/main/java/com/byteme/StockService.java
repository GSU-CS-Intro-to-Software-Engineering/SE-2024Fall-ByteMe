package com.byteme;

import com.byteme.DataRetreival.NumericalDataFetcher;
import com.byteme.DataRetreival.StockNewsFetcher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {

    public Map<String, Object> gatherStockData(String symbol) {
        Map<String, Object> result = new HashMap<>();
        int[] newsSentiment = gatherNewsSentiment(symbol);
        Map<String, Object> indicatorData = gatherIndicatorData(symbol);

        if (newsSentiment == null || indicatorData == null) {
            result.put("error", "Failed to gather stock data.");
            return result;
        }

        boolean uploadSuccess = uploadDataToDatabase(symbol, newsSentiment, indicatorData);

        result.put("uploadSuccess", uploadSuccess);
        result.put("symbol", symbol);
        result.put("positiveSentiment", newsSentiment[0]);
        result.put("neutralSentiment", newsSentiment[1]);
        result.put("negativeSentiment", newsSentiment[2]);

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

            // Convert the JsonObject into a Map<String, Object>
            Map<String, Object> indicatorData = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : indicatorJson.entrySet()) {
                String key = entry.getKey().toLowerCase(); // Convert to match database column names
                JsonObject value = entry.getValue().getAsJsonObject();

                // Map fields directly to their column names in the database
                for (Map.Entry<String, JsonElement> field : value.entrySet()) {
                    String fieldKey = field.getKey().toLowerCase(); // e.g., "ema", "macd", "upper_band"
                    indicatorData.put(fieldKey, field.getValue().getAsString());
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

}
