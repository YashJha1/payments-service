package com.eci.paymentsservice.service;

import com.eci.paymentsservice.dto.PaymentRequest;
import com.eci.paymentsservice.dto.PaymentResponse;

import java.util.UUID;
import java.util.List;

public interface PaymentService {
    PaymentResponse charge(PaymentRequest request);
    PaymentResponse getPayment(UUID id);
    PaymentResponse refund(UUID id, String idempotencyKey);
    List<PaymentResponse> getAllPayments();
}

