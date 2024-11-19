package com.byteme;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AlpacaController {

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) {
        String apiKey = credentials.get("apiKey");
        String apiSecret = credentials.get("apiSecret");
        String tradingType = credentials.get("tradingType"); // Get tradingType from request

        // Determine the Alpaca API endpoint based on the trading type
        String alpacaAccountUrl;
        if ("cash".equalsIgnoreCase(tradingType)) {
            alpacaAccountUrl = "https://api.alpaca.markets/v2/account"; // Live trading endpoint
        } else {
            alpacaAccountUrl = "https://paper-api.alpaca.markets/v2/account"; // Paper trading endpoint
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("APCA-API-KEY-ID", apiKey);
            headers.set("APCA-API-SECRET-KEY", apiSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(alpacaAccountUrl, HttpMethod.GET, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("Authenticated");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API credentials");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API credentials");
        }
    }

    @PostMapping("/execute-trade")
    public ResponseEntity<?> executeTrade(@RequestBody Map<String, Object> tradeDetails) {
        String apiKey = (String) tradeDetails.get("apiKey");
        String apiSecret = (String) tradeDetails.get("apiSecret");
        String tradingType = (String) tradeDetails.get("tradingType");
        String stockSymbol = (String) tradeDetails.get("stockSymbol");
        String action = (String) tradeDetails.get("action"); // "buy" or "sell"
        int quantity = (int) tradeDetails.get("quantity");

        // Determine the Alpaca trading endpoint based on the trading type
        String alpacaOrdersUrl;
        if ("cash".equalsIgnoreCase(tradingType)) {
            alpacaOrdersUrl = "https://api.alpaca.markets/v2/orders"; // Live trading endpoint
        } else {
            alpacaOrdersUrl = "https://paper-api.alpaca.markets/v2/orders"; // Paper trading endpoint
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("APCA-API-KEY-ID", apiKey);
            headers.set("APCA-API-SECRET-KEY", apiSecret);

            // Create the trade order JSON body
            Map<String, Object> order = Map.of(
                    "symbol", stockSymbol,
                    "qty", quantity,
                    "side", action,
                    "type", "market",
                    "time_in_force", "gtc");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(order, headers);
            ResponseEntity<String> response = restTemplate.exchange(alpacaOrdersUrl, HttpMethod.POST, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                return ResponseEntity.ok("Trade executed successfully: " + response.getBody());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to execute trade: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while executing the trade.");
        }
    }
}
