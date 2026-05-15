package com.jysk.interview.service;

import com.jysk.interview.domain.enums.OrderStatus;
import com.jysk.interview.domain.model.Order;
import com.jysk.interview.domain.model.OrderItem;
import com.jysk.interview.dto.request.CreateOrderRequest;
import com.jysk.interview.dto.response.OrderResponse;
import com.jysk.interview.exception.InventoryException;
import com.jysk.interview.exception.ProductException;
import com.jysk.interview.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PricingService pricingService;

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void init() {
        orderService = new OrderService(inventoryService, pricingService, orderRepository);
    }

    @Test
    void shouldProcessOrderSuccessfully() {
        final CreateOrderRequest createOrderRequest = getCreateOrderRequest();
        when(pricingService.calculateTotal(createOrderRequest.items()))
                .thenReturn(BigDecimal.valueOf(15.7));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(getOrder(createOrderRequest));

        final OrderResponse orderResponse = orderService.processOrder(createOrderRequest);
        assertNotNull(orderResponse);
        assertEquals(OrderStatus.SUCCESS, orderResponse.getStatus());
        assertNull(orderResponse.getMessages());
        assertEquals(BigDecimal.valueOf(15.7), orderResponse.getTotalAmount());
    }

    @Test
    void shouldReturnFailedResponseWhenPriceCalculationFails() {
        final CreateOrderRequest createOrderRequest = getCreateOrderRequest();

        when(pricingService.calculateTotal(createOrderRequest.items()))
                .thenThrow(new ProductException("Product not found"));

        final OrderResponse orderResponse = orderService.processOrder(createOrderRequest);
        assertNotNull(orderResponse);
        assertEquals(OrderStatus.FAILED, orderResponse.getStatus());
        assertNotNull(orderResponse.getMessages());
        assertTrue(orderResponse.getMessages().contains("Product not found"));
    }

    @Test
    void shouldReturnFailedResponseWhenInventoryReservationFails() {
        final CreateOrderRequest createOrderRequest = getCreateOrderRequest();

        when(pricingService.calculateTotal(createOrderRequest.items()))
                .thenReturn(BigDecimal.valueOf(15.7));
        doThrow(new InventoryException("Insufficient stock"))
                .when(inventoryService).reserve(createOrderRequest.items());

        final OrderResponse orderResponse = orderService.processOrder(createOrderRequest);
        assertNotNull(orderResponse);
        assertEquals(OrderStatus.FAILED, orderResponse.getStatus());
        assertNotNull(orderResponse.getMessages());
        assertTrue(orderResponse.getMessages().contains("Insufficient stock"));
    }

    private static Order getOrder(CreateOrderRequest createOrderRequest) {
        return new Order("order-1", "customer-1", createOrderRequest.items(),
                BigDecimal.valueOf(15.7), LocalDateTime.now());
    }

    private static CreateOrderRequest getCreateOrderRequest() {
        List<OrderItem> orderItems = List.of(
                new OrderItem("product-1", 1),
                new OrderItem("product-2", 2),
                new OrderItem("product-3", 3));
        return new CreateOrderRequest("customer-1", orderItems);
    }
}
