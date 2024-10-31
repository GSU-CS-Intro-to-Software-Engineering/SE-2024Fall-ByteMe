package com.byteme;

import com.google.gson.JsonObject;

public class TradingStrategy {

    private static final double HIGH_THRESHOLD = 1.05;
    private static final double LOW_THRESHOLD = 0.95;
    private static final double MAX_POSITION_SIZE = 1000.0;

    // Analyze stock data and return an action: Buy, Sell, or Hold
    public String analyze(JsonObject numericalData) {
        double latestClose = getLatestClosePrice(numericalData);
        double movingAverage = calculateMovingAverage(numericalData, 5);

        if (latestClose > movingAverage * HIGH_THRESHOLD) {
            return "Sell";
        } else if (latestClose < movingAverage * LOW_THRESHOLD) {
            return "Buy";
        } else {
            return "Hold";
        }
    }

    // Calculate the position size based on volatility
    public double calculatePositionSize(JsonObject numericalData) {
        double volatility = calculateVolatility(numericalData);
        double basePosition = MAX_POSITION_SIZE / volatility;
        return Math.min(basePosition, MAX_POSITION_SIZE);
    }

    // Helper: Get the latest close price from the data
    private double getLatestClosePrice(JsonObject numericalData) {
        return numericalData.entrySet().stream()
                .findFirst()
                .map(entry -> entry.getValue().getAsJsonObject().get("4. close").getAsDouble())
                .orElse(0.0);
    }

    // Helper: Calculate moving average for the given days
    private double calculateMovingAverage(JsonObject numericalData, int days) {
        return numericalData.entrySet().stream()
                .limit(days)
                .mapToDouble(entry -> entry.getValue().getAsJsonObject().get("4. close").getAsDouble())
                .average()
                .orElse(0.0);
    }

    // Helper: Calculate volatility based on high/low prices
    private double calculateVolatility(JsonObject numericalData) {
        double highestHigh = numericalData.entrySet().stream()
                .mapToDouble(entry -> entry.getValue().getAsJsonObject().get("2. high").getAsDouble())
                .max()
                .orElse(0.0);

        double lowestLow = numericalData.entrySet().stream()
                .mapToDouble(entry -> entry.getValue().getAsJsonObject().get("3. low").getAsDouble())
                .min()
                .orElse(0.0);

        return highestHigh - lowestLow;
    }
}


//wazzaappp