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
public class AuthenticationController {

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
}
