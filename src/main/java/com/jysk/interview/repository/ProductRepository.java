package com.jysk.interview.repository;

import com.jysk.interview.domain.model.Product;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductRepository {

    private final Map<String, Product> productsLookup = new HashMap<>();

    public Optional<Product> findById(final String productId) {
        return Optional.ofNullable(productsLookup.get(productId));
    }

    public void save(final Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot save null product");
        }

        productsLookup.put(product.id(), product);
    }

    public List<Product> findAllById(final Set<String> productIds) {
        return productsLookup.entrySet()
                .stream()
                .filter(productEntry -> productIds.contains(productEntry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    public List<Product> findAll() {
        return List.copyOf(productsLookup.values());
    }
}
