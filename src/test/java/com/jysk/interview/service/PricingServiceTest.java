package com.jysk.interview.service;

import com.jysk.interview.domain.model.OrderItem;
import com.jysk.interview.domain.model.Product;
import com.jysk.interview.exception.ProductException;
import com.jysk.interview.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private ProductRepository productRepository;
    private PricingService pricingService;

    @BeforeEach
    void init() {
        pricingService = new PricingService(productRepository);
    }

    @Test
    void shouldCalculateTotalPriceSuccessfully() {
        final Set<String> productIds = getValidProductIds();
        final List<Product> products = getValidProducts();
        final List<OrderItem> orderItems = getValidOrderItems();

        when(productRepository.findAllById(productIds)).thenReturn(products);

        BigDecimal calculatedTotal = pricingService.calculateTotal(orderItems);
        assertEquals(BigDecimal.valueOf(17.0), calculatedTotal);
    }

    @Test
    void shouldThrowProductExceptionWhenProductDoesNotExist() {
        final List<OrderItem> orderItems = getInvalidOrderItems();

        when(productRepository.findAllById(anySet())).thenReturn(List.of());

        assertThrows(ProductException.class, () -> pricingService.calculateTotal(orderItems));
    }

    private static List<OrderItem> getValidOrderItems() {
        return List.of(
                new OrderItem("product-1", 1),
                new OrderItem("product-2", 2),
                new OrderItem("product-3", 3));
    }

    private static List<OrderItem> getInvalidOrderItems() {
        return List.of(
                new OrderItem("invalid-product-id", 5));
    }

    private static List<Product> getValidProducts() {
        return List.of(
                new Product("product-1", "Product 1", BigDecimal.valueOf(1.5)),
                new Product("product-2", "Product 2", BigDecimal.valueOf(2.5)),
                new Product("product-3", "Product 3", BigDecimal.valueOf(3.5)));
    }

    private static Set<String> getValidProductIds() {
        return Set.of("product-1", "product-2", "product-3");
    }
}
