package com.byteme;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // manual testing (redacted)
    @GetMapping("/api/trade")
    public Map<String, Object> startTrading(@RequestParam String symbol) {
        return stockService.gatherStockData(symbol);
    }

    @GetMapping("/uploadCounter")
    public ResponseEntity<Integer> getUploadCounter() {
        int uploadCounter = stockService.getUploadCounter();
        return ResponseEntity.ok(uploadCounter);
    }

}
