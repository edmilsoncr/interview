package com.jysk.interview.domain.model;

public record Inventory(
        String productId,
        int availableQuantity
) {}
