package com.byteme;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sc")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/fetchPortfolioData")
    public ResponseEntity<Map<String, Object>> fetchData() {
        Map<String, Object> data = stockService.getPortfolioData();
        System.out.println("\nData fetched by portfolio.html: " + data + "\n"); // Debug print
        System.out.println("Data.recentTrades: " + data.get("recentTrades") + "\n"); // Debug print
        return ResponseEntity.ok(data);
    }

    @GetMapping("/toggleTrader")
    public ResponseEntity<String> toggleTrader() {
        stockService.toggleTrader();
        String status = stockService.isTradingEnabled() ? "Trading Enabled" : "Trading Disabled";
        System.out.println("Toggle endpoint hit. New status: " + status); // Debug print
        return ResponseEntity.ok(status);
    }

    @PostMapping("/update-stock")
    public ResponseEntity<Boolean> updateStock(@RequestBody Map<String, String> stockMap) {
        String symbol = stockMap.get("selectedStock"); // Retrieve selectedStock
        boolean success = stockService.setSymbol(symbol); // Update symbol

        return ResponseEntity.ok(success);
    }

    @PostMapping("/uploadTradeData")
    public ResponseEntity<String> uploadTradeData(@RequestBody Map<String, Object> tradeDetails) {
        System.out.println("Received trade details: " + tradeDetails);

        // Extract fields from the JSON payload
        String symbol = (String) tradeDetails.get("symbol");
        String action = (String) tradeDetails.get("action");
        String quantity = tradeDetails.get("quantity").toString();
        String price = tradeDetails.get("price").toString();
        String info = (String) tradeDetails.get("info");

        // Upload trade data to the database
        boolean isUploaded = stockService.uploadTradeDataToDatabase(symbol, new String[] { action, quantity, price },
                info);

        if (isUploaded) {
            return ResponseEntity.ok("Trade details uploaded successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Failed to upload trade details.");
        }
    }
}
