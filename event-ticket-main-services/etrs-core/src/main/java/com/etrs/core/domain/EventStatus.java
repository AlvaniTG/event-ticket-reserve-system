package com.etrs.core.domain;

public enum EventStatus {
    REQUESTED,  // Czeka na akceptację
    APPROVED,   // Zaakceptowane do sprzedaży
    REJECTED,   // Odrzucone przez moderatora
    CHANGED,    // Zmieniono detale
    CANCELED,   // Odwołane
    ONGOING,    // Właśnie trwa
    FINISHED;   // Zakończone

    public boolean canTransitionTo(EventStatus status) {
        return switch (this) {
            case REQUESTED -> status == APPROVED || status == REJECTED;
            case APPROVED -> status == REJECTED || status == CANCELED || status == CHANGED || status == ONGOING;
            case CHANGED -> status == APPROVED || status == REJECTED || status == CANCELED;
            case ONGOING -> status == FINISHED;
            case REJECTED, CANCELED, FINISHED -> false;
        };
    }

    public EventStatus nextStatusOnReschedule() {
        return this == APPROVED ? CHANGED : this;
    }
}
