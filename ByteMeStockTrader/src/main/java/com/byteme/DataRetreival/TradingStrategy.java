package com.byteme.DataRetreival;

import com.google.gson.JsonObject;

public class TradingStrategy {

    // Define thresholds for decision-making
    private static final double RSI_OVERBOUGHT = 70.0;
    private static final double RSI_OVERSOLD = 30.0;
    private static final double SMA_THRESHOLD = 1.02; // SMA comparison multiplier
    private static final double MACD_SIGNAL_DIFF = 0.0; // MACD line vs Signal line threshold

    public String analyze(JsonObject smaData, JsonObject rsiData, JsonObject macdData) {
        try {
            // Extract values from the indicators
            double currentSMA = extractIndicatorValue(smaData, "Technical Analysis: SMA", "SMA");
            double currentRSI = extractIndicatorValue(rsiData, "Technical Analysis: RSI", "RSI");
            double macdValue = extractIndicatorValue(macdData, "Technical Analysis: MACD", "MACD");
            double signalValue = extractIndicatorValue(macdData, "Technical Analysis: MACD", "Signal");

            // Decision-making based on indicators
            if (currentRSI < RSI_OVERSOLD && macdValue > signalValue) {
                return "Buy"; // RSI indicates oversold and MACD confirms upward trend
            } else if (currentRSI > RSI_OVERBOUGHT || macdValue < signalValue) {
                return "Sell"; // RSI indicates overbought or MACD confirms downward trend
            } else if (currentSMA * SMA_THRESHOLD < macdValue) {
                return "Hold"; // Prices are within a stable range relative to SMA
            } else {
                return "Hold"; // Default fallback
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Hold"; // Return a conservative recommendation on error
        }
    }

    // Helper to extract indicator values from JSON response
    private double extractIndicatorValue(JsonObject json, String keyPath, String valueKey) {
        JsonObject indicators = json.getAsJsonObject(keyPath);
        String latestDate = indicators.keySet().iterator().next(); // Get the most recent date
        return indicators.getAsJsonObject(latestDate).get(valueKey).getAsDouble();
    }
}
