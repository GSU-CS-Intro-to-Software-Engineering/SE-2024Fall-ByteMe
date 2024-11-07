package com.byteme;

import com.byteme.DataRetreival.NumericalDataFetcher;
import com.byteme.DataRetreival.StockNewsFetcher;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {

    public Map<String, Object> gatherStockData(String symbol) {
        Map<String, Object> result = new HashMap<>();
        int[] newsSentiment = gatherNewsSentiment(symbol);
        int[] numericData = gatherNumericData(symbol);

        if (newsSentiment == null || numericData == null) {
            result.put("error", "Failed to gather stock data.");
            return result;
        }

        boolean uploadSuccess = uploadDataToDatabase(symbol, newsSentiment, numericData);
        result.put("uploadSuccess", uploadSuccess);
        result.put("symbol", symbol);
        result.put("positiveSentiment", newsSentiment[0]);
        result.put("neutralSentiment", newsSentiment[1]);
        result.put("negativeSentiment", newsSentiment[2]);
        result.put("averageOpen", numericData[0]);
        result.put("averageHigh", numericData[1]);
        result.put("averageLow", numericData[2]);
        result.put("averageClose", numericData[3]);
        result.put("averageVolume", numericData[4]);

        return result;
    }

    public static int[] gatherNewsSentiment(String symbol) {

        try {

            // Instantiate the StockNewsFetcher with selected symbol
            StockNewsFetcher newsFetcher = new StockNewsFetcher(symbol);

            // Fetch stock article titles
            ArrayList<String> titles = newsFetcher.fetchYahooFinanceApiTitles();
            System.out.println("-" + titles.size() + " articles fetched from Yahoo Finance API.\n");

            // TODO: Add more sources here (News API, Alpha Vantage, etc.)

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

    public static int[] gatherNumericData(String symbol) {
        try {
            NumericalDataFetcher dataFetcher = new NumericalDataFetcher(symbol);
            System.out.println("-Fetching numerical data for " + symbol + " . . .");
            JsonObject numericalData = dataFetcher.fetchData();

            if (numericalData == null) {
                System.out.println("Failed to fetch numeric data. Exiting...");
                return null;
            }

            // Convert values from JSON to int array
            int[] numericValues = {
                    (int) numericalData.get("averageOpen").getAsDouble(),
                    (int) numericalData.get("averageHigh").getAsDouble(),
                    (int) numericalData.get("averageLow").getAsDouble(),
                    (int) numericalData.get("averageClose").getAsDouble(),
                    numericalData.get("averageVolume").getAsInt()
            };

            return numericValues;

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean uploadDataToDatabase(String symbol, int[] newsSentiment, int[] numericData) {

        try {
            // Create an instance of DatabaseHandler to handle database operations
            DatabaseHandler dbHandler = new DatabaseHandler();

            // Initialize the table (if not already created)
            dbHandler.initializeTable();

            // Insert mock data to test the connection and insertion functionality
            Date currentDate = new Date(System.currentTimeMillis());

            // Print the data to be inserted
            System.out.println("\nInserting data for " + symbol + " on " + currentDate + "...");
            System.out.println("News sentiment: Positive - " + newsSentiment[0] + ", Neutral - " + newsSentiment[1]
                    + ", Negative - " + newsSentiment[2]);
            System.out.println("Numeric data: Open - " + numericData[0] + ", High - " + numericData[1] + ", Low - "
                    + numericData[2] + ", Close - " + numericData[3] + ", Volume - " + numericData[4] + "\n");

            // Insert or update the mock data
            dbHandler.insertOrUpdateData(currentDate, symbol, newsSentiment[0], newsSentiment[1], newsSentiment[2],
                    numericData[0], numericData[1], numericData[2], numericData[3], numericData[4]);

            // Close the database connection
            dbHandler.closeConnection();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
