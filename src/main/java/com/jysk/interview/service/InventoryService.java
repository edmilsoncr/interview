package com.jysk.interview.service;

import com.jysk.interview.domain.model.OrderItem;
import com.jysk.interview.exception.InventoryException;
import com.jysk.interview.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * Reserves the given items from stock.
     * If the reservation fails, a compensation operation is executed.
     *
     * @param items contains the details of the product to reserve.
     */
    public void reserve(final List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }

        final List<OrderItem> successfulReservations = new ArrayList<>();

        try {
            for (OrderItem item : items) {
                inventoryRepository.reserve(item.productId(), item.quantity());
                successfulReservations.add(item);
            }
        } catch (InventoryException ex) {
            rollbackReservations(successfulReservations);
            throw ex;
        }
    }

    private void rollbackReservations(final List<OrderItem> items) {
        items.forEach(item -> inventoryRepository
                .release(item.productId(), item.quantity()));
    }
}
