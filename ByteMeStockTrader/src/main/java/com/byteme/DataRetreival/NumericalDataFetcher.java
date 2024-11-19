package com.byteme.DataRetreival;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NumericalDataFetcher {

    private final String symbol;
    private final String ALPHA_VANTAGE_API_KEY = System.getenv("ALPHA_VANTAGE_API_KEY");

    public NumericalDataFetcher(String symbol) {
        this.symbol = symbol;
    }

    // Fetch Simple Moving Average (SMA)
    public JsonObject fetchSMA(int timePeriod) throws Exception {
        String url = "https://www.alphavantage.co/query?function=SMA&symbol="
                + symbol + "&interval=daily&time_period=" + timePeriod + "&series_type=close&apikey=" + ALPHA_VANTAGE_API_KEY;

        return fetchData(url);
    }

    // Fetch Relative Strength Index (RSI)
    public JsonObject fetchRSI(int timePeriod) throws Exception {
        String url = "https://www.alphavantage.co/query?function=RSI&symbol="
                + symbol + "&interval=daily&time_period=" + timePeriod + "&series_type=close&apikey=" + ALPHA_VANTAGE_API_KEY;

        return fetchData(url);
    }

    // Fetch Moving Average Convergence Divergence (MACD)
    public JsonObject fetchMACD() throws Exception {
        String url = "https://www.alphavantage.co/query?function=MACD&symbol="
                + symbol + "&interval=daily&series_type=close&apikey=" + ALPHA_VANTAGE_API_KEY;

        return fetchData(url);
    }

    // Generic method to fetch data from a given URL
    private JsonObject fetchData(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Error: Unable to fetch data. Status Code: " + response.statusCode());
            return null;
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
