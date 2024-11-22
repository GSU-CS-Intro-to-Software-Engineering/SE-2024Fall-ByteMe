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

    private static String apiKey;
    private static String apiSecret;
    private static String tradingType;
    private static String symbol;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) {
        apiKey = credentials.get("apiKey");
        apiSecret = credentials.get("apiSecret");
        tradingType = credentials.get("tradingType");

        // Validate credentials by calling the Alpaca API
        String alpacaAccountUrl = tradingType.equalsIgnoreCase("cash")
                ? "https://api.alpaca.markets/v2/account"
                : "https://paper-api.alpaca.markets/v2/account";

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("APCA-API-KEY-ID", apiKey);
            headers.set("APCA-API-SECRET-KEY", apiSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(alpacaAccountUrl, HttpMethod.GET, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("Authenticated successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API credentials");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during authentication.");
        }
    }

    @GetMapping("/current-symbol")
    public ResponseEntity<String> getCurrentSymbol() {
        if (symbol != null && !symbol.isEmpty()) {
            return ResponseEntity.ok(symbol);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No symbol set.");
        }
    }

    @PostMapping("/update-stock")
    public ResponseEntity<?> updateStock(@RequestBody Map<String, String> stockMap) {
        try {
            System.out.println("Received payload: " + stockMap); // Debug log
            symbol = stockMap.get("selectedStock"); // Retrieve selectedStock
            System.out.println("Updated symbol: " + symbol); // Confirm it was updated
            return ResponseEntity.ok("Stock symbol updated successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // Log the full exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update stock symbol.");
        }
    }

    @PostMapping("/execute-trade")
    public ResponseEntity<?> executeTrade(@RequestBody Map<String, Object> tradeDetails) {
        if (apiKey == null || apiSecret == null || tradingType == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated. Please log in.");
        }

        String action = (String) tradeDetails.get("action"); // "buy" or "sell"
        int quantity = (int) tradeDetails.get("quantity");

        String alpacaOrdersUrl = tradingType.equalsIgnoreCase("cash")
                ? "https://api.alpaca.markets/v2/orders"
                : "https://paper-api.alpaca.markets/v2/orders";

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("APCA-API-KEY-ID", apiKey);
            headers.set("APCA-API-SECRET-KEY", apiSecret);

            Map<String, Object> order = Map.of(
                    "symbol", symbol,
                    "side", action,
                    "qty", quantity,
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
                    .body("An error occurred while executing the trade. quantity: " + quantity + " action: " + action
                            + " symbol: " + symbol + " error: "
                            + e.getMessage());
        }
    }

    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolioData() {
        if (apiKey == null || apiSecret == null || tradingType == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated. Please log in.");
        }

        String alpacaAccountUrl = tradingType.equalsIgnoreCase("cash")
                ? "https://api.alpaca.markets/v2/account"
                : "https://paper-api.alpaca.markets/v2/account";

        String alpacaPositionsUrl = tradingType.equalsIgnoreCase("cash")
                ? "https://api.alpaca.markets/v2/positions"
                : "https://paper-api.alpaca.markets/v2/positions";

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Fetch account balance
            HttpHeaders headers = new HttpHeaders();
            headers.set("APCA-API-KEY-ID", apiKey);
            headers.set("APCA-API-SECRET-KEY", apiSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> accountResponse = restTemplate.exchange(alpacaAccountUrl, HttpMethod.GET, entity,
                    Map.class);
            ResponseEntity<Map[]> positionsResponse = restTemplate.exchange(alpacaPositionsUrl, HttpMethod.GET, entity,
                    Map[].class);

            if (accountResponse.getStatusCode() == HttpStatus.OK
                    && positionsResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> portfolioData = Map.of(
                        "account", accountResponse.getBody(),
                        "positions", positionsResponse.getBody());
                return ResponseEntity.ok(portfolioData);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to fetch portfolio data.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching portfolio data: " + e.getMessage());
        }
    }

}
