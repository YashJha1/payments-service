package com.eci.paymentsservice.controller;

import com.eci.paymentsservice.dto.PaymentRequest;
import com.eci.paymentsservice.dto.PaymentResponse;
import com.eci.paymentsservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;  // ✅ ensure this line exists

    @PostMapping("/charge")
    public PaymentResponse charge(@RequestBody PaymentRequest req,
                                  @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        req.setIdempotencyKey(idempotencyKey);
        return service.charge(req);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return service.getPayment(id);
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable UUID id,
                                  @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.refund(id, idempotencyKey);
    }

    // ✅ New GET endpoint to view payments in browser
    @GetMapping
    public List<PaymentResponse> getAllPayments() {
        return service.getAllPayments();
    }

    // ✅ Browser health endpoint
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "Payments Service is running");
    }
}

