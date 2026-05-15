package com.jysk.interview.dto.response;

import com.jysk.interview.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
        private String id;
        private BigDecimal totalAmount;
        private OrderStatus status;
        private List<String> messages;
}
