package com.eci.paymentsservice.service.impl;

import com.eci.paymentsservice.dto.*;
import com.eci.paymentsservice.model.Payment;
import com.eci.paymentsservice.repository.PaymentRepository;
import com.eci.paymentsservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repo;

    @Override
    @Transactional
    public PaymentResponse charge(PaymentRequest req) {
        if (req.getIdempotencyKey() != null) {
            var existing = repo.findByIdempotencyKey(req.getIdempotencyKey());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

        Payment p = Payment.builder()
               // .paymentId(UUID.randomUUID())
                .orderId(req.getOrderId())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .paymentMethod(req.getPaymentMethod())
                .status("PENDING")
                .idempotencyKey(req.getIdempotencyKey())
                .build();

        p = repo.save(p);

        // Simulate success from provider
        p.setStatus("COMPLETED");
        p.setProviderTransactionId("PROV-" + UUID.randomUUID());
        repo.save(p);

        return toResponse(p);
    }

    @Override
    public PaymentResponse getPayment(UUID id) {
        return repo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    @Transactional
    public PaymentResponse refund(UUID id, String idempotencyKey) {
        Payment p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("REFUNDED".equals(p.getStatus())) {
            return toResponse(p);
        }

        p.setStatus("REFUNDED");
        p.setProviderTransactionId("REF-" + UUID.randomUUID());
        repo.save(p);

        return toResponse(p);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getPaymentId())
                .orderId(p.getOrderId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .providerTransactionId(p.getProviderTransactionId())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

