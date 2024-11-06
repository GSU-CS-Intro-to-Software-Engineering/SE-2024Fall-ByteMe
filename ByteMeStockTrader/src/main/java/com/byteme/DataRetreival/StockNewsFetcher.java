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

    // other API keys...
    // private final String NEWS_API_KEY = "your_newsapi_key_here";
    // private final String ALPHA_VANTAGE_KEY = "your_alpha_vantage_key_here";

    // Fetch articles using Yahoo Finance API DATA (RapidAPI)
    public ArrayList<String> fetchYahooFinanceApiTitles() throws Exception {

        System.out.println("\n-Using Yahoo Finance API to fetch news related to " + symbol + " . . . \n");

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

    /*
     * // Fetch Nvidia-related articles from NewsAPI.org
     * public JsonArray fetchNewsAPIArticles() throws Exception {
     * String url = "https://newsapi.org/v2/everything?q=Nvidia&apiKey=" +
     * NEWS_API_KEY;
     * HttpRequest request = HttpRequest.newBuilder()
     * .uri(URI.create(url))
     * .build();
     * HttpResponse<String> response = client.send(request,
     * HttpResponse.BodyHandlers.ofString());
     * JsonObject jsonResponse =
     * JsonParser.parseString(response.body()).getAsJsonObject();
     * return jsonResponse.getAsJsonArray("articles");
     * }
     * 
     * // Fetch articles from Alpha Vantage (requires premium for News function)
     * public JsonArray fetchAlphaVantageArticles() throws Exception {
     * String url =
     * "https://www.alphavantage.co/query?function=NEWS_SENTIMENT&tickers=NVDA&apikey="
     * + ALPHA_VANTAGE_KEY;
     * HttpRequest request = HttpRequest.newBuilder()
     * .uri(URI.create(url))
     * .build();
     * HttpResponse<String> response = client.send(request,
     * HttpResponse.BodyHandlers.ofString());
     * JsonObject jsonResponse =
     * JsonParser.parseString(response.body()).getAsJsonObject();
     * return jsonResponse.getAsJsonArray("news");
     * }
     * 
     * 
     * // Example function to parse RSS (you can expand it to get title and link)
     * private JsonArray parseRSSFeed(String rssFeed) {
     * // Implement RSS parsing logic here (if you want structured JSON output from
     * // Yahoo RSS)
     * return new JsonArray();
     * }
     */
}
// wazzaappp
