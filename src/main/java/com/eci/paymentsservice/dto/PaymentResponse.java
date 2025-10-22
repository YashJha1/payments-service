/* package com.eci.paymentsservice.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID paymentId;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String providerTransactionId;
    private OffsetDateTime createdAt;
}
*/

package com.eci.paymentsservice.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID paymentId;
    private String orderId;
    private BigDecimal amount;
    private String currency;

    // ✅ must match entity & service
    private String paymentMethod;

    private String status;
    private String providerTransactionId;
    private OffsetDateTime createdAt;
}


