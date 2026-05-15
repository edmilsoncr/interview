package com.jysk.interview.repository;

import com.jysk.interview.domain.model.Inventory;
import com.jysk.interview.exception.InventoryException;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InventoryRepository {

    private final Map<String, Inventory> inventoryLookup = new ConcurrentHashMap<>();

    /**
     * Reserves the provided amount of product by subtracting it from the total items
     * mapped to the provided productId in the inventory store.
     *
     * @param productId the id of product to be reserved.
     * @param quantity the amount of product to be reserved.
     */
    public void reserve(final String productId, final int quantity) {
        if (quantity <= 0) {
            throw new InventoryException(
                    String.format("Invalid quantity provided: %s. Quantity has to be a positive integer", productId));
        }

        // Atomic update of the inventory ensuring thread-safe reservation per product
        inventoryLookup.compute(productId, (id, inventory) -> {
            if (inventory == null) {
                throw new InventoryException(
                        String.format("Inventory not found for product with id %s", productId));
            }

            if (inventory.availableQuantity() < quantity) {
                throw new InventoryException(
                        String.format("Insufficient stock for product with id %s", productId));
            }

            return new Inventory(id, inventory.availableQuantity() - quantity);
        });
    }

    /**
     * Releases the provided amount of product to the stock by adding it to the current
     * total amount of items mapped to the provided productId.
     *
     * @param productId the id of product to be released.
     * @param quantity the amount of product to be released.
     */
    public void release(final String productId, final int quantity) {
        inventoryLookup.compute(productId, (id, inventory) -> {
            if (inventory == null) {
                throw new InventoryException(String.format("Inventory not found for product with id %s", productId));
            }

            return new Inventory(id, inventory.availableQuantity() + quantity);
        });
    }

    public Optional<Inventory> findByProductId(final String productId) {
        return Optional.ofNullable(inventoryLookup.get(productId));
    }

    public void save(final Inventory inventory) {
        inventoryLookup.put(inventory.productId(), inventory);
    }
}
