package com.jysk.interview.domain.model;

public record OrderItem(
        String productId,
        int quantity
) {}
