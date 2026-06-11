package com.etrs.orderservice.service;

import com.etrs.orderservice.client.EventServiceClient;
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
    private final EventServiceClient eventServiceClient;

    public Mono<OrderDto.Response> createOrder(OrderDto.CreateRequest request, UUID userId) {

        return eventServiceClient.fetchEventDetails(request.eventId())
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

                    log.info("=============================================");
                    log.info("NOTIFICATION SYSTEM -> Generating e-ticket for order with id {}", event.orderId());
                    log.info("NOTIFICATION SYSTEM -> Sending email to the user with id {}", order.getUserId());
                    log.info("NOTIFICATION SYSTEM -> Email sent successfully.");
                    log.info("=============================================");

                    return orderRepository.save(order);
                })
                .then();
    }
}
