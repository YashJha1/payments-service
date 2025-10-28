package com.eci.paymentservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * PaymentController - CSV-backed simple payments endpoints.
 *
 * - GET /v1/payments                -> returns all payments (or filtered by ?orderId=)
 * - GET /v1/payments/order/{orderId} -> returns payments for a given orderId
 *
 * This implementation reads payments from a CSV (auto-detects LOCAL vs DOCKER path),
 * and returns List<Map<String,String>> so there's no dependency on DTO classes that may
 * be missing during compile-time.
 */
@RestController
@RequestMapping("/v1/payments")
@Slf4j
public class PaymentController {

    private static final String LOCAL_PATH = "/home/yash/ECI-Microservices/Infra/init/payments/eci_payments.csv";
    private static final String DOCKER_PATH = "/app/init/payments/eci_payments.csv";

    @GetMapping
    public ResponseEntity<?> getPayments(@RequestParam(required = false) String orderId) {
        try {
            List<Map<String, String>> all = readPaymentsCsv();

            if (orderId != null && !orderId.isEmpty()) {
                List<Map<String, String>> filtered = filterByOrderId(all, orderId);
                if (filtered.isEmpty()) {
                    return ResponseEntity.status(404)
                            .body(Map.of("message", "No payments found for order_id: " + orderId));
                }
                return ResponseEntity.ok(filtered);
            }

            return ResponseEntity.ok(all);
        } catch (Exception e) {
            log.error("Error reading CSV: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns payments for the given orderId (path param).
     * Uses the same CSV reader (no external service or DTO dependency).
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentsByOrder(@PathVariable String orderId) {
        try {
            List<Map<String, String>> all = readPaymentsCsv();
            List<Map<String, String>> filtered = filterByOrderId(all, orderId);
            if (filtered.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "No payments found for order_id: " + orderId));
            }
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            log.error("Error reading CSV for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // -------------------------
    // Helper methods
    // -------------------------

    private List<Map<String, String>> readPaymentsCsv() throws Exception {
        List<Map<String, String>> paymentsList = new ArrayList<>();

        // Auto-detect file path (local vs Docker)
        String csvPath = new java.io.File(LOCAL_PATH).exists() ? LOCAL_PATH : DOCKER_PATH;
        var fileResource = new FileSystemResource(csvPath);

        if (!fileResource.exists()) {
            throw new IllegalStateException("File not found at " + csvPath);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(fileResource.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return Collections.emptyList();
            }

            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1); // keep empties
                Map<String, String> record = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    record.put(headers[i].trim(), i < values.length ? values[i].trim() : "");
                }

                // add optional parsed/typed fields if you want (e.g. createdAt parsing)
                // record.putIfAbsent("fetchedAt", LocalDateTime.now().toString());

                paymentsList.add(record);
            }
        }

        return paymentsList;
    }

    private List<Map<String, String>> filterByOrderId(List<Map<String, String>> all, String orderId) {
        List<Map<String, String>> filtered = new ArrayList<>();
        for (Map<String, String> payment : all) {
            if (payment.getOrDefault("order_id", "").equals(orderId)) {
                filtered.add(payment);
            }
        }
        return filtered;
    }

    /**
 * POST /v1/payments
 * Simulates payment processing triggered from Orders service.
 */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> paymentRequest) {
    	try {
        	// Extract fields
        	String orderId = String.valueOf(paymentRequest.get("orderId"));
        	String amount = String.valueOf(paymentRequest.get("amount"));
        	String method = String.valueOf(paymentRequest.getOrDefault("method", "UPI"));
        	String status = "SUCCESS";
        	String reference = "TXN-" + UUID.randomUUID();
        	String createdAt = LocalDateTime.now().toString();

        	// Create record
        	Map<String, String> newPayment = new LinkedHashMap<>();
        	newPayment.put("payment_id", String.valueOf(new Random().nextInt(10000)));
        	newPayment.put("order_id", orderId);
        	newPayment.put("amount", amount);
        	newPayment.put("method", method);
        	newPayment.put("status", status);
        	newPayment.put("reference", reference);
        	newPayment.put("created_at", createdAt);

        	// Log and respond
        	log.info("✅ Payment created: {}", newPayment);

        	// Optionally: append to CSV (for persistence simulation)
        	appendToCsv(newPayment);

        	return ResponseEntity.ok(newPayment);

    	} catch (Exception e) {
        	log.error("Error creating payment: {}", e.getMessage(), e);
        	return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    	}
    }

    private void appendToCsv(Map<String, String> newPayment) {
    	try {
        	String csvPath = new java.io.File(LOCAL_PATH).exists() ? LOCAL_PATH : DOCKER_PATH;
        	java.nio.file.Files.writeString(
                	java.nio.file.Paths.get(csvPath),
                	String.join(",", newPayment.values()) + System.lineSeparator(),
                	StandardCharsets.UTF_8,
                	java.nio.file.StandardOpenOption.APPEND
        	);
        	log.info("Payment appended to CSV: {}", csvPath);
    	} catch (Exception e) {
        	log.warn("Could not append payment to CSV: {}", e.getMessage());
    	}
    }

}

