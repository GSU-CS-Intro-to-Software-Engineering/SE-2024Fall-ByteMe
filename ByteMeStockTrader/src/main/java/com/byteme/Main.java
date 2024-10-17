package com.byteme;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

//import com.google.gson.JsonArray;

public class Main {

    public static void main(String[] args) {

        // (1) UI for the user to input the stock symbol (Ate)

        // (2) Instantiate the StockNewsFetcher with selected symbol
        StockNewsFetcher newsFetcher = new StockNewsFetcher("NVDA");

        try {

            // (3) Fetch stock article titles
            ArrayList<String> titles = newsFetcher.fetchYahooFinanceApiTitles();
            // *** Add more sources here (News API, Alpha Vantage, etc.) ***

            // (4) Build the command to execute Python script for sentiment analysis
            String workingDir = System.getProperty("user.dir");
            String[] sentimentCommand = { "python3", workingDir + "/ByteMeStockTrader/src/python/sentiment.py" };

            System.out.println("WorkingDir: " + workingDir);

            // Create a process to run the Python script
            ProcessBuilder titlesSentimentAnalyzer = new ProcessBuilder(sentimentCommand);
            Process process = titlesSentimentAnalyzer.start();

            // Write the article titles to Python process input
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            for (String title : titles) {
                writer.write(title);
                writer.newLine();
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
    }
}
