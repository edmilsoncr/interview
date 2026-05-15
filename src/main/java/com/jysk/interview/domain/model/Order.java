package com.jysk.interview.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Order (
        String id,
        String customerId,
        List<OrderItem> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {}
