package com.jysk.interview.service;

import com.jysk.interview.domain.model.OrderItem;
import com.jysk.interview.exception.InventoryException;
import com.jysk.interview.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    private InventoryService inventoryService;

    @BeforeEach
    void init() {
        inventoryService = new InventoryService(inventoryRepository);
    }

    @Test
    void shouldReserveAllItemsSuccessfully() {
        List<OrderItem> validOrderItems = getValidOrderItems();
        inventoryService.reserve(validOrderItems);

        verify(inventoryRepository, times(validOrderItems.size())).reserve(anyString(), anyInt());
        verify(inventoryRepository, times(0)).release(anyString(), anyInt());
    }

    @Test
    void shouldRollbackPreviousReservationsWhenReservationFails() {
        List<OrderItem> validOrderItems = getValidOrderItems();

        doNothing()
                .doThrow(new InventoryException("Insufficient stock"))
                .when(inventoryRepository).reserve(anyString(), anyInt());

        assertThrows(InventoryException.class, () -> inventoryService.reserve(validOrderItems));
        verify(inventoryRepository, times(2)).reserve(anyString(), anyInt());
        verify(inventoryRepository, times(1)).release(anyString(), anyInt());
    }

    private static List<OrderItem> getValidOrderItems() {
        return List.of(
                new OrderItem("product-1", 1),
                new OrderItem("product-2", 2),
                new OrderItem("product-3", 3));
    }
}
