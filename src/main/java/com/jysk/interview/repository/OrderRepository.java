package com.jysk.interview.repository;

import com.jysk.interview.domain.model.Order;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final Map<String, Order> ordersLookup = new HashMap<>();

    public Order save(final Order order) {
        ordersLookup.put(order.id(), order);
        return order;
    }

    public Optional<Order> findById(final String orderId) {
        return Optional.ofNullable(ordersLookup.get(orderId));
    }

    public List<Order> findAll() {
        return List.copyOf(ordersLookup.values());
    }
}
