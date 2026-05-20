package com.etrs.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID creatorId;

    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    public void verifyOwnership(UUID requesterId) {
        if (!this.creatorId.equals(requesterId)) {
            throw new AccessDeniedException("You cannot edit an event that you did not create!");
        }
    }

    public void changeStatusAfterReschedule() {
        EventStatus targetStatus = this.status.nextStatusOnReschedule();

        if (!this.status.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("Niedozwolona zmiana statusu!");
        }

        this.status = targetStatus;
    }
}
