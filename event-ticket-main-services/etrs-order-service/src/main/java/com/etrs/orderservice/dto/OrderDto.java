package com.etrs.orderservice.dto;

import com.etrs.orderservice.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderDto {
    record CreateRequest(
            @NotNull UUID eventId
    ) {}

    record Response(
            UUID id,
            UUID eventId,
            OrderStatus status
    ) {}

    record EventDetailsResponse(
            UUID id,
            BigDecimal price
    ) {}
}
