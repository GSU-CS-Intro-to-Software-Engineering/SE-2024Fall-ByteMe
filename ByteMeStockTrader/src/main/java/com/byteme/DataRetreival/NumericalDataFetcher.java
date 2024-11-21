package com.byteme.DataRetreival;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

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

        // Fetch Open/Close/High/Low/Volume
        JsonObject openCloseHighLowVolumeData = fetchOpenCloseHighLowVolume();
        if (openCloseHighLowVolumeData != null) {
            allIndicators.add("OPEN_CLOSE_HIGH_LOW_VOLUME", openCloseHighLowVolumeData);
        } else {
            System.err.println("Error fetching Open/Close/High/Low/Volume.");
        }

        // Fetch Indicators
        for (String function : functions) {
            String url = buildUrl(function);
            JsonObject latestData = fetchLatestValue(url, function);

            if (latestData != null) {
                allIndicators.add(function.toUpperCase(), latestData);
            }
        }

        return allIndicators;
    }

    // Fetch Open, Prior Close, High, Low, and Volume
    private JsonObject fetchOpenCloseHighLowVolume() throws Exception {
        String url = "https://api.twelvedata.com/time_series" +
                "?symbol=" + symbol +
                "&interval=1day&outputsize=2&apikey=" + TWELVE_DATA_API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println(
                    "Error: Unable to fetch Open/Close/High/Low/Volume. Status Code: " + response.statusCode());
            return null;
        }

        String responseBody = response.body();
        System.out.println("Open/Close/High/Low/Volume Response: " + responseBody);

        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        if (jsonResponse.has("values")) {
            JsonArray values = jsonResponse.get("values").getAsJsonArray();
            JsonObject openCloseHighLowVolumeData = new JsonObject();

            // Most recent day data
            JsonObject mostRecentDay = values.get(0).getAsJsonObject();
            openCloseHighLowVolumeData.addProperty("open", mostRecentDay.get("open").getAsString());
            openCloseHighLowVolumeData.addProperty("high", mostRecentDay.get("high").getAsString());
            openCloseHighLowVolumeData.addProperty("low", mostRecentDay.get("low").getAsString());
            openCloseHighLowVolumeData.addProperty("close", mostRecentDay.get("close").getAsString());
            openCloseHighLowVolumeData.addProperty("volume", mostRecentDay.get("volume").getAsString());

            // Prior day's close
            if (values.size() > 1) {
                JsonObject priorDay = values.get(1).getAsJsonObject();
                openCloseHighLowVolumeData.addProperty("previous_close", priorDay.get("close").getAsString());
            } else {
                openCloseHighLowVolumeData.addProperty("previous_close", "N/A");
            }

            return openCloseHighLowVolumeData;
        }

        System.err.println("Error: Missing values in time series response.");
        return null;
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

        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        if (jsonResponse.has("values")) {
            for (JsonElement entry : jsonResponse.get("values").getAsJsonArray()) {
                JsonObject dataPoint = entry.getAsJsonObject();
                dataPoint.remove("datetime"); // Remove the datetime field
                return dataPoint;
            }
        } else if (jsonResponse.has("value")) {
            JsonObject latestValue = new JsonObject();
            latestValue.addProperty("value", jsonResponse.get("value").getAsString());
            return latestValue;
        }

        System.err.println("Error: Missing data for " + function);
        return null;
    }
}
