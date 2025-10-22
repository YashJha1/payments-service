/* package com.eci.paymentsservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private String paymentMethod;

    private String idempotencyKey;
}

*/


package com.eci.paymentsservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank
    private String orderId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String paymentMethod;

    private String idempotencyKey;
}

