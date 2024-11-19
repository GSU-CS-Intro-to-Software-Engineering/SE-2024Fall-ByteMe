package com.byteme.DataRetreival;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NumericalDataFetcher {

    private final String symbol;
    private final String TWELVE_DATA_API_KEY = "b53098d10889404fa04fc5092646877b"; // System.getenv("TWELVE_DATA_API_KEY");

    public NumericalDataFetcher(String symbol) {
        this.symbol = symbol;
    }

    // Fetch all indicators and their latest values
    public JsonObject fetchIndicators() throws Exception {
        String[] functions = { "sma", "ema", "rsi", "macd", "bbands", "obv", "atr" };
        JsonObject allIndicators = new JsonObject();

        for (String function : functions) {
            String url = buildUrl(function);
            JsonObject latestData = fetchLatestValue(url, function);

            if (latestData != null) {
                allIndicators.add(function.toUpperCase(), latestData);
            }
        }

        return allIndicators;
    }

    // Build the API URL for each indicator
    private String buildUrl(String function) {
        String baseUrl = "https://api.twelvedata.com/";
        String interval = "1h"; // Hourly data
        switch (function) {
            case "sma":
                return baseUrl + "sma?symbol=" + symbol +
                        "&interval=" + interval + "&time_period=14&apikey=" + TWELVE_DATA_API_KEY;
            case "ema":
                return baseUrl + "ema?symbol=" + symbol +
                        "&interval=" + interval + "&time_period=14&apikey=" + TWELVE_DATA_API_KEY;
            case "rsi":
                return baseUrl + "rsi?symbol=" + symbol +
                        "&interval=" + interval + "&time_period=14&apikey=" + TWELVE_DATA_API_KEY;
            case "macd":
                return baseUrl + "macd?symbol=" + symbol +
                        "&interval=" + interval + "&apikey=" + TWELVE_DATA_API_KEY;
            case "bbands":
                return baseUrl + "bbands?symbol=" + symbol +
                        "&interval=" + interval + "&time_period=20&apikey=" + TWELVE_DATA_API_KEY;
            case "obv":
                return baseUrl + "obv?symbol=" + symbol +
                        "&interval=" + interval + "&apikey=" + TWELVE_DATA_API_KEY;
            case "atr":
                return baseUrl + "atr?symbol=" + symbol +
                        "&interval=" + interval + "&time_period=14&apikey=" + TWELVE_DATA_API_KEY;
            default:
                return null;
        }
    }

    // Fetch the latest value of an indicator
    private JsonObject fetchLatestValue(String url, String function) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err
                    .println("Error: Unable to fetch data for " + function + ". Status Code: " + response.statusCode());
            return null;
        }

        String responseBody = response.body();
        System.out.println("Response for " + function + ": " + responseBody);

        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        if (jsonResponse.has("values")) {
            for (JsonElement entry : jsonResponse.get("values").getAsJsonArray()) {
                JsonObject dataPoint = entry.getAsJsonObject();
                // Return the first (most recent) data point
                dataPoint.remove("datetime"); // Remove the datetime field
                return dataPoint;
            }
        } else if (jsonResponse.has("value")) {
            // Handle simpler cases like OBV, ATR, etc.
            JsonObject latestValue = new JsonObject();
            latestValue.addProperty("value", jsonResponse.get("value").getAsString());
            return latestValue;
        }

        System.err.println("Error: Missing data for " + function);
        return null;
    }

}
