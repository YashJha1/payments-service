package com.eci.paymentservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/v1/payments")
@Slf4j
public class PaymentController {

    private static final String LOCAL_PATH = "/home/yash/ECI-Microservices/Infra/init/payments/eci_payments.csv";
    private static final String DOCKER_PATH = "/app/init/payments/eci_payments.csv";

    @GetMapping
    public ResponseEntity<?> getPayments(@RequestParam(required = false) String orderId) {
        List<Map<String, String>> paymentsList = new ArrayList<>();

        try {
            // ✅ Auto-detect correct file path
            String csvPath = new java.io.File(LOCAL_PATH).exists() ? LOCAL_PATH : DOCKER_PATH;
            var fileResource = new FileSystemResource(csvPath);

            if (!fileResource.exists()) {
                return ResponseEntity.internalServerError().body(Map.of("error", "File not found at " + csvPath));
            }

            // ✅ Read CSV contents
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(fileResource.getInputStream(), StandardCharsets.UTF_8))) {

                String headerLine = reader.readLine();
                if (headerLine == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }

                String[] headers = headerLine.split(",");
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    Map<String, String> record = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        record.put(headers[i].trim(), i < values.length ? values[i].trim() : "");
                    }
                    paymentsList.add(record);
                }
            }

            // ✅ If orderId is provided, filter results
            if (orderId != null && !orderId.isEmpty()) {
                List<Map<String, String>> filtered = new ArrayList<>();
                for (Map<String, String> payment : paymentsList) {
                    if (payment.getOrDefault("order_id", "").equals(orderId)) {
                        filtered.add(payment);
                    }
                }

                if (filtered.isEmpty()) {
                    return ResponseEntity.status(404)
                            .body(Map.of("message", "No payments found for order_id: " + orderId));
                }

                return ResponseEntity.ok(filtered);
            }

            // ✅ Return all payments if no orderId specified
            return ResponseEntity.ok(paymentsList);

        } catch (Exception e) {
            log.error("Error reading CSV: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

