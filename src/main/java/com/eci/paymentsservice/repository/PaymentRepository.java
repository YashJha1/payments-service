package com.eci.paymentsservice.repository;

import com.eci.paymentsservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    /*Optional<Payment> findByOrderId(UUID orderId); */
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}

