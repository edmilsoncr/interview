package com.jysk.interview.repository;

import com.jysk.interview.domain.model.Inventory;
import com.jysk.interview.exception.InventoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class InventoryRepositoryTest {

    private InventoryRepository inventoryRepository;

    @BeforeEach
    void init() {
        inventoryRepository = new InventoryRepository();
    }

    @Test
    void shouldReserveInventoryWhenStockIsAvailable() {
        final String productId = "product-1";
        addInventory(productId, 5);

        inventoryRepository.reserve(productId, 3);

        Optional<Inventory> productInventory = inventoryRepository.findByProductId(productId)
                .filter(inventory -> productId.equals(inventory.productId()));
        assertTrue(productInventory.isPresent());
        assertEquals(2, productInventory.get().availableQuantity());
    }

    @Test
    void shouldThrowInventoryExceptionWhenStockIsInsufficient() {
        final String productId = "product-2";
        addInventory(productId, 1);

        assertThrows(InventoryException.class,
                () -> inventoryRepository.reserve(productId, 3),
                String.format("Insufficient stock for product with id %s", productId));
    }

    @Test
    void shouldAllowOnlyOneReservationWhenStockIsInsufficientForConcurrentRequests() {
        final String productId = "product-3";
        addInventory(productId, 10);

        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            CountDownLatch startLatch = new CountDownLatch(1);

            Callable<Boolean> reservationTask = () -> {
                startLatch.await();
                try {
                    inventoryRepository.reserve(productId, 8);
                    return true;
                } catch (InventoryException ex) {
                    return false;
                }
            };

            Future<Boolean> firstRequest = executorService.submit(reservationTask);
            Future<Boolean> secondRequest = executorService.submit(reservationTask);

            startLatch.countDown();

            boolean firstResult = firstRequest.get();
            boolean secondResult = secondRequest.get();

            long successfulReservations = Stream.of(firstResult, secondResult)
                    .filter(Boolean::booleanValue)
                    .count();

            assertEquals(1, successfulReservations);

            executorService.shutdown();
        } catch (ExecutionException | InterruptedException ex) {
            fail("Concurrent execution failed unexpectedly", ex);
        }
    }

    private void addInventory(final String productId, final int quantity) {
        inventoryRepository.save(new Inventory(productId, quantity));
    }
}
