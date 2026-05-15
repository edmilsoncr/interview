package com.jysk.interview.service;

import com.jysk.interview.domain.enums.OrderStatus;
import com.jysk.interview.domain.model.Order;
import com.jysk.interview.domain.model.OrderItem;
import com.jysk.interview.dto.request.CreateOrderRequest;
import com.jysk.interview.dto.response.OrderResponse;
import com.jysk.interview.exception.InventoryException;
import com.jysk.interview.exception.OrderException;
import com.jysk.interview.exception.ProductException;
import com.jysk.interview.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final OrderRepository orderRepository;

    /**
     * Orchestrates the steps of the order processing:
     * <ul>
     *   <li>validation
     *   <li>price calculation
     *   <li>inventory reservation
     *   <li>persistence of the order
     *
     * @param request the {@link CreateOrderRequest} containing the details of the order.
     * @return the {@link OrderResponse} containing the status of the operation.
     */
    public OrderResponse processOrder(final CreateOrderRequest request) {
        try {
            validateItems(request.items());
            final BigDecimal totalAmount = pricingService.calculateTotal(request.items());
            inventoryService.reserve(request.items());
            final Order order = orderRepository.save(buildOrder(request, totalAmount));
            return buildSuccessResponse(order, totalAmount);
        } catch (InventoryException | ProductException | OrderException ex) {
            log.error("Failed to process Order for customer {}", request.customerId(), ex);
            return OrderResponse.builder()
                    .status(OrderStatus.FAILED)
                    .messages(List.of(ex.getMessage()))
                    .build();
        }
    }

    private void validateItems(final List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderException("Items cannot be empty");
        }

        for (OrderItem item : items) {
            if (item.quantity() <= 0) {
                throw new OrderException(
                        String.format("Order items has to be a positive integer: %s", item.quantity()));
            }

            if (item.productId() == null || item.productId().isBlank()) {
                throw new OrderException("Order item cannot have empty product id");
            }
        }
    }

    private static Order buildOrder(final CreateOrderRequest request, final BigDecimal totalAmount) {
        return new Order(UUID.randomUUID().toString(), request.customerId(),
                request.items(), totalAmount, LocalDateTime.now());
    }

    private static OrderResponse buildSuccessResponse(final Order order, final BigDecimal totalAmount) {
        return OrderResponse.builder()
                .id(order.id())
                .totalAmount(totalAmount)
                .status(OrderStatus.SUCCESS)
                .build();
    }
}
