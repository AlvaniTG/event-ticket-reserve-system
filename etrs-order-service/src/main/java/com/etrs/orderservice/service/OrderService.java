package com.etrs.orderservice.service;

import com.etrs.orderservice.domain.Order;
import com.etrs.orderservice.domain.OrderStatus;
import com.etrs.orderservice.dto.OrderDto;
import com.etrs.orderservice.dto.PaymentEvent;
import com.etrs.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient eventServiceClient;

    public Mono<OrderDto.Response> createOrder(OrderDto.CreateRequest request, UUID userId) {

        return fetchEventDetails(userId)
                .flatMap(eventDetails -> {
                    Order order = new Order();
                    order.setEventId(request.eventId());
                    order.setUserId(userId);

                    if (eventDetails.price().compareTo(BigDecimal.ZERO) == 0) {
                        order.setStatus(OrderStatus.COMPLETED);
                    } else {
                        order.setStatus(OrderStatus.PENDING);
                    }

                    return orderRepository.save(order);
                })
                .map(savedOrder -> new OrderDto.Response(
                        savedOrder.getId(),
                        savedOrder.getEventId(),
                        savedOrder.getStatus()
                ));
    }

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

    public Mono<Void> processPayment(PaymentEvent event) {
        if (!"SUCCESS".equals(event.paymentStatus())) {
            log.warn("Ignored Payment with status: {} for order with id: {}", event.paymentStatus(), event.orderId());
            return Mono.empty();
        }

        return orderRepository.findById(event.orderId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Order with id " + event.orderId() + " not found.")))
                .flatMap(order -> {
                    if (order.getStatus() == OrderStatus.COMPLETED) {
                        log.info("Order with id {} has been already completed", event.orderId());
                        return Mono.empty();
                    }
                    if (order.getStatus() != OrderStatus.PENDING) {
                        return Mono.error(new IllegalArgumentException("Invalid order status " + order.getId() + ": " + order.getStatus()));
                    }
                    order.setStatus(OrderStatus.COMPLETED);
                    return orderRepository.save(order);
                })
                .then();
    }
}
