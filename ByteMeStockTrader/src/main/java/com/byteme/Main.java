package com.byteme;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.google.gson.JsonObject;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        // (1) UI for the user to input the stock symbol (Ate)

        // (2) Instantiate the StockNewsFetcher with selected symbol
        StockNewsFetcher newsFetcher = new StockNewsFetcher("NVDA");

        try { //Daniel's code

            // (3) Fetch stock article titles
            ArrayList<String> titles = newsFetcher.fetchYahooFinanceApiTitles();
            // *** Add more sources here (News API, Alpha Vantage, etc.) ***

            // (4) Build the command to execute Python script for sentiment analysis
            String workingDir = System.getProperty("user.dir");
            String[] sentimentCommand = { "python3", workingDir + "/ByteMeStockTrader/src/python/sentiment.py" };

            // System.out.println("WorkingDir: " + workingDir);

            System.out.println("-" + titles.size() + " articles fetched from Yahoo Finance API.\n");

            // Create a process to run the Python script
            ProcessBuilder titlesSentimentAnalyzer = new ProcessBuilder(sentimentCommand);
            Process process = titlesSentimentAnalyzer.start();

            // Write the article titles to Python process input
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            for (String title : titles) {
                writer.write(title);
                writer.newLine();

                System.out.println("Article: " + title);

            }
            writer.close();

            // Read the Python script output (sentiment scores)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the Python process to complete
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try { //Corbin's code
            // Step 1: Initialize the NumericalDataFetcher with a stock symbol
            String stockSymbol = "NVDA"; // Example symbol
            NumericalDataFetcher dataFetcher = new NumericalDataFetcher(stockSymbol);

            // Step 2: Fetch numerical data
            System.out.println("Fetching numerical data for " + stockSymbol + "...");
            JsonObject numericalData = dataFetcher.fetchData();

            if (numericalData == null) {
                logger.severe("Failed to fetch numerical data. Exiting.");
                return;
            }

            // Step 3: Initialize TradingStrategy
            TradingStrategy tradingStrategy = new TradingStrategy();

            // Step 4: Analyze the fetched data
            System.out.println("\nAnalyzing data for trading strategy...");
            String action = tradingStrategy.analyze(numericalData);
            double amountToTrade = tradingStrategy.calculatePositionSize(numericalData);

            // Step 5: Display the recommended action and amount
            System.out.println("Recommended Action for " + stockSymbol + ": " + action);
            System.out.println("Recommended Amount to Trade: " + amountToTrade);

        } catch (Exception e) {
            logger.severe("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
//wazzaappp