package com.etrs.orderservice.client;

import com.etrs.orderservice.dto.OrderDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventServiceClient {

    private final WebClient eventServiceClient;

    @CircuitBreaker(name = "eventServiceBreaker", fallbackMethod = "eventServiceFallback")
    public Mono<OrderDto.EventDetailsResponse> fetchEventDetails(UUID eventId) {
        return eventServiceClient.get()
                .uri("/api/events/{id}", eventId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, _ ->
                        Mono.error(new IllegalArgumentException("No event with given id exists in the catalog.")))
                .bodyToMono(OrderDto.EventDetailsResponse.class);
    }

    public Mono<OrderDto.EventDetailsResponse> eventServiceFallback(UUID eventId, Throwable throwable) {
        log.error("Circuit Breaker activated for event {}. Reason: {}", eventId, throwable.getMessage());
        return Mono.error(new IllegalStateException("Catalog service cannot be reached. Try again later."));
    }
}
