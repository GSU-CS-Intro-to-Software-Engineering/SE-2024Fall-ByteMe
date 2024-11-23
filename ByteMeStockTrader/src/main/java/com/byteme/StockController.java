package com.byteme;

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

    @GetMapping("/fetchData")
    public ResponseEntity<Map<String, Object>> fetchData() {
        System.out.println("Fetching bulk data endpoint hit."); // Debug print
        Map<String, Object> data = stockService.getPortfolioData();
        System.out.println("\nData fetched by portfolio.html: " + data + "\n"); // Debug print
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

}
