package com.etrs.orderservice.dto;

import java.util.UUID;

public record PaymentEvent(
        UUID orderId,
        String paymentStatus
) {
}
