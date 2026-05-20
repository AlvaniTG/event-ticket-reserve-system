package com.etrs.orderservice.service;

import com.etrs.orderservice.domain.Order;
import com.etrs.orderservice.domain.OrderStatus;
import com.etrs.orderservice.dto.OrderDto;
import com.etrs.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient eventServiceClient;

    public Mono<OrderDto.Response> createOrder(OrderDto.CreateRequest request, UUID userId) {

        return eventServiceClient.get()
                .uri("/api/events/{id}", request.eventId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, _ ->
                        Mono.error(new IllegalArgumentException("No event with given id exists in the catalog.")))
                .bodyToMono(OrderDto.EventDetailsResponse.class)
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
}
