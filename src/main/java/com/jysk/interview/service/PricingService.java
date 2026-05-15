package com.jysk.interview.service;

import com.jysk.interview.domain.model.OrderItem;
import com.jysk.interview.domain.model.Product;
import com.jysk.interview.exception.ProductException;
import com.jysk.interview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final ProductRepository productRepository;

    /**
     * Calculates the total price of the items.
     *
     * @param items a list of {@link OrderItem} containing the details of each item of the order.
     * @return the total price calculated.
     */
    public BigDecimal calculateTotal(final List<OrderItem> items) {
        final Set<String> productIds = items.stream()
                .map(OrderItem::productId)
                .collect(Collectors.toSet());
        final Map<String, Product> productsLookup = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));

        return items.stream()
                .map(item -> getTotalItemPrice(item, productsLookup.get(item.productId())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalItemPrice(final OrderItem item, final Product product) {
        if (product == null) {
            throw new ProductException(String.format("Product with id %s not found", item.productId()));
        }

        return product.price()
                .multiply(BigDecimal.valueOf(item.quantity()));
    }
}
