package com.byteme.DataRetreival;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;

public class StockNewsFetcher {

    private String symbol;

    public StockNewsFetcher(String symbol) {
        this.symbol = symbol;
    }

    private final String YAHOO_FINANCE_API_KEY = System.getenv("YAHOO_FINANCE_API_KEY");

    // Fetch articles using Yahoo Finance API DATA (RapidAPI)
    public ArrayList<String> fetchYahooFinanceApiTitles() throws Exception {

        System.out.println("-Using Yahoo Finance API to fetch news related to " + symbol + " . . . \n");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://yahoo-finance-api-data.p.rapidapi.com/news/list?symbol=" + symbol + "&limit=10"))
                .header("x-rapidapi-key", YAHOO_FINANCE_API_KEY)
                .header("x-rapidapi-host", "yahoo-finance-api-data.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        // Extract titles from the "stream" array under "data.main.stream"
        ArrayList<String> titles = new ArrayList<>();
        if (jsonResponse.has("data") && jsonResponse.getAsJsonObject("data").has("main")) {
            JsonArray streamArray = jsonResponse.getAsJsonObject("data").getAsJsonObject("main")
                    .getAsJsonArray("stream");
            for (int i = 0; i < streamArray.size(); i++) {
                JsonObject article = streamArray.get(i).getAsJsonObject().getAsJsonObject("content");
                String title = article.get("title").getAsString();
                titles.add(title);
            }
        }

        return titles; // Return the list of article titles
    }

}
