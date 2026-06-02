package com.etrs.core.dto;

import com.etrs.core.domain.EventStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EventDto {
    record CreateRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull @Future LocalDateTime startDate,
            @Future LocalDateTime endDate,
            @NotNull @PositiveOrZero BigDecimal price
    ) {
        public CreateRequest {
            if (endDate != null && endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
        }
    }

    record UpdateRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotNull @PositiveOrZero BigDecimal price
    ) {}

    record RescheduleRequest(
            @NotNull @Future LocalDateTime newStartDate,
            @Future LocalDateTime newEndDate,
            @NotBlank String reason
    ) {
        public RescheduleRequest {
            if (newEndDate != null && newEndDate.isBefore(newStartDate)) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
        }
    }

    record SummaryResponse(
            UUID id,
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate,
            EventStatus status
    ) {}

    record DetailsResponse(
            UUID id,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal price,
            EventStatus status
    ) {}
}
