package com.etrs.orderservice.messaging;

import com.etrs.orderservice.dto.PaymentEvent;
import com.etrs.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;

    @Bean
    public Consumer<PaymentEvent> paymentEvents() {
        return event -> {
            log.info("Received PaymentEvent from RabbitMQ: Payment for order {}", event.orderId());

            orderService.processPayment(event).subscribe(
                    null,
                    error -> log.error("Error processing Payment for order {}: {}", event.orderId(), error.getMessage()),
                    () -> log.info("Payment for order {} has been processed", event.orderId())
            );
        };
    }
}
