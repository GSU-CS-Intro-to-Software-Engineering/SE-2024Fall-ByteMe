package com.byteme.DataRetreival;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NumericalDataFetcher {

    private final String symbol;
    private final String ALPHA_VANTAGE_API_KEY = System.getenv("ALPHA_VANTAGE_API_KEY");
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public NumericalDataFetcher(String symbol) {
        this.symbol = symbol;
    }

    public JsonObject fetchData() throws Exception {
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="
                + symbol + "&apikey=" + ALPHA_VANTAGE_API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Error: Unable to fetch numerical data. Status Code: " + response.statusCode());
            return null;
        }

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject timeSeries = jsonResponse.getAsJsonObject("Time Series (Daily)");

        System.out.println("\nNumerical Data for " + symbol + " (Past 3 Weeks):");

        double totalOpen = 0, totalHigh = 0, totalLow = 0, totalClose = 0;
        int totalVolume = 0, count = 0;

        LocalDate today = LocalDate.now();
        LocalDate threeWeeksAgo = today.minusWeeks(3);

        for (Map.Entry<String, JsonElement> entry : timeSeries.entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());

            if (date.isBefore(threeWeeksAgo)) {
                continue; // Skip data older than 3 weeks
            }

            JsonObject dayData = entry.getValue().getAsJsonObject();

            double open = dayData.get("1. open").getAsDouble();
            double high = dayData.get("2. high").getAsDouble();
            double low = dayData.get("3. low").getAsDouble();
            double close = dayData.get("4. close").getAsDouble();
            int volume = dayData.get("5. volume").getAsInt();

            if (volume < 0) {
                System.err.println("Warning: Negative volume encountered on " + date + ": " + volume);
                volume = 0;
            }

            System.out.println(date + " -> Open: " + open + ", High: " + high +
                    ", Low: " + low + ", Close: " + close + ", Volume: " + volume);

            totalOpen += open;
            totalHigh += high;
            totalLow += low;
            totalClose += close;
            totalVolume += volume;
            count++;
        }

        /*
         * if (count > 0) {
         * System.out.println("\n3-Week Averages:");
         * System.out.println("Average Open: " + df.format(totalOpen / count));
         * System.out.println("Average High: " + df.format(totalHigh / count));
         * System.out.println("Average Low: " + df.format(totalLow / count));
         * System.out.println("Average Close: " + df.format(totalClose / count));
         * System.out.println("Average Volume: " + totalVolume / count);
         * } else {
         * System.out.println("No data available for the past 3 weeks.");
         * }
         */

        return timeSeries;
    }
}
