package com.etrs.orderservice.repository;

import com.etrs.orderservice.domain.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {
}
