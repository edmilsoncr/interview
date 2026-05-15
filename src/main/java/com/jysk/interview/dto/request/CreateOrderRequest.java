package com.jysk.interview.dto.request;

import com.jysk.interview.domain.model.OrderItem;

import java.util.List;

public record CreateOrderRequest(
        String customerId,
        List<OrderItem> items
) {}
