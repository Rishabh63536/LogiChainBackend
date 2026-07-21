package com.cts.logichain360;

import com.cts.logichain360.controller.ProductWarehouseController;
import com.cts.logichain360.dto.request.LaunchProductAtWarehouseRequest;
import com.cts.logichain360.dto.request.RestockRequest;
import com.cts.logichain360.dto.request.UpdateProductWarehouseRequest;
import com.cts.logichain360.dto.response.ProductWarehouseResponse;
import com.cts.logichain360.service.ProductWarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductWarehouseController using Mockito.
 * Covers: launch, getById, getByProductId, getByWarehouseId, getAll,
 *         getLowStock, getLowStockByWarehouse, updateThresholds, restock, delete
 */
@ExtendWith(MockitoExtension.class)
class ProductWarehouseControllerTest {

    @Mock
    private ProductWarehouseService productWarehouseService;

    @InjectMocks
    private ProductWarehouseController productWarehouseController;

    private ProductWarehouseResponse normalStockEntry;
    private ProductWarehouseResponse lowStockEntry;
    private LaunchProductAtWarehouseRequest launchRequest;
    private UpdateProductWarehouseRequest updateRequest;
    private RestockRequest restockRequest;

    @BeforeEach
    void setUp() {
        normalStockEntry = ProductWarehouseResponse.builder()
                .id(1L).productId(5L).productName("Sony WH-1000XM5 Headphones")
                .productPrice(29990.0).vendorId(30L).vendorCompanyName("Sony India Pvt Ltd")
                .warehouseId(100L).warehouseCode("WH-CHN-01").warehouseLocation("Chennai")
                .stock(300).maxStock(500).rolPercent(40.0)
                .currentStockPercent(60.0).belowRol(false)
                .build();

        lowStockEntry = ProductWarehouseResponse.builder()
                .id(2L).productId(6L).productName("LG Monitor")
                .productPrice(15000.0).vendorId(31L).vendorCompanyName("LG India")
                .warehouseId(100L).warehouseCode("WH-CHN-01").warehouseLocation("Chennai")
                .stock(90).maxStock(500).rolPercent(40.0)
                .currentStockPercent(18.0).belowRol(true)
                .build();

        launchRequest = LaunchProductAtWarehouseRequest.builder()
                .productId(5L).warehouseId(100L)
                .stock(300).maxStock(500).rolPercent(40.0)
                .build();

        updateRequest = UpdateProductWarehouseRequest.builder()
                .maxStock(600).rolPercent(35.0)
                .build();

        restockRequest = RestockRequest.builder()
                .amount(100)
                .build();
    }

    // ─── launch ───────────────────────────────────────────────────────────────

    @Test
    void launch_ShouldReturnCreatedEntry_WhenValidRequest() {
        when(productWarehouseService.launch(any(LaunchProductAtWarehouseRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(normalStockEntry));

        ResponseEntity<ProductWarehouseResponse> response = productWarehouseController.launch(launchRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getProductId());
        assertEquals(100L, response.getBody().getWarehouseId());
        assertEquals(300, response.getBody().getStock());
        verify(productWarehouseService, times(1)).launch(any(LaunchProductAtWarehouseRequest.class));
    }

    @Test
    void launch_ShouldReturnConflict_WhenProductAlreadyLaunchedAtWarehouse() {
        when(productWarehouseService.launch(any(LaunchProductAtWarehouseRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        ResponseEntity<ProductWarehouseResponse> response = productWarehouseController.launch(launchRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void launch_ShouldReturnNotFound_WhenProductOrWarehouseNotFound() {
        when(productWarehouseService.launch(any(LaunchProductAtWarehouseRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<ProductWarehouseResponse> response = productWarehouseController.launch(launchRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnEntry_WhenExists() {
        when(productWarehouseService.getById(1L)).thenReturn(ResponseEntity.ok(normalStockEntry));

        ResponseEntity<ProductWarehouseResponse> response = productWarehouseController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertFalse(response.getBody().getBelowRol());
        verify(productWarehouseService, times(1)).getById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenEntryDoesNotExist() {
        when(productWarehouseService.getById(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<ProductWarehouseResponse> response = productWarehouseController.getById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getByProductId ───────────────────────────────────────────────────────

    @Test
    void getByProductId_ShouldReturnEntry_WhenProductExists() {
        when(productWarehouseService.getByProductId(5L)).thenReturn(ResponseEntity.ok(normalStockEntry));

        ResponseEntity<ProductWarehouseResponse> response = productWarehouseController.getByProductId(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().getProductId());
        verify(productWarehouseService, times(1)).getByProductId(5L);
    }

    @Test
    void getByProductId_ShouldReturnNotFound_WhenProductHasNoStockEntry() {
        when(productWarehouseService.getByProductId(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<ProductWarehouseResponse> response = productWarehouseController.getByProductId(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getByWarehouseId ─────────────────────────────────────────────────────

    @Test
    void getByWarehouseId_ShouldReturnAllEntriesAtWarehouse() {
        List<ProductWarehouseResponse> entries = Arrays.asList(normalStockEntry, lowStockEntry);
        when(productWarehouseService.getByWarehouseId(100L)).thenReturn(ResponseEntity.ok(entries));

        ResponseEntity<List<ProductWarehouseResponse>> response =
                productWarehouseController.getByWarehouseId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        response.getBody().forEach(e -> assertEquals(100L, e.getWarehouseId()));
        verify(productWarehouseService, times(1)).getByWarehouseId(100L);
    }

    @Test
    void getByWarehouseId_ShouldReturnEmptyList_WhenWarehouseHasNoProducts() {
        when(productWarehouseService.getByWarehouseId(200L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<ProductWarehouseResponse>> response =
                productWarehouseController.getByWarehouseId(200L);

        assertTrue(response.getBody().isEmpty());
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllStockEntries() {
        List<ProductWarehouseResponse> all = Arrays.asList(normalStockEntry, lowStockEntry);
        when(productWarehouseService.getAll()).thenReturn(ResponseEntity.ok(all));

        ResponseEntity<List<ProductWarehouseResponse>> response = productWarehouseController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(productWarehouseService, times(1)).getAll();
    }

    // ─── getLowStock ──────────────────────────────────────────────────────────

    @Test
    void getLowStock_ShouldReturnOnlyBelowRolEntries() {
        List<ProductWarehouseResponse> lowStockEntries = Collections.singletonList(lowStockEntry);
        when(productWarehouseService.getLowStock()).thenReturn(ResponseEntity.ok(lowStockEntries));

        ResponseEntity<List<ProductWarehouseResponse>> response = productWarehouseController.getLowStock();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getBelowRol());
        verify(productWarehouseService, times(1)).getLowStock();
    }

    @Test
    void getLowStock_ShouldReturnEmptyList_WhenAllStockIsAboveRol() {
        when(productWarehouseService.getLowStock()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<ProductWarehouseResponse>> response = productWarehouseController.getLowStock();

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getLowStock_ShouldNotCallGetAll() {
        when(productWarehouseService.getLowStock()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        productWarehouseController.getLowStock();

        verify(productWarehouseService).getLowStock();
        verify(productWarehouseService, never()).getAll();
    }

    // ─── getLowStockByWarehouse ───────────────────────────────────────────────

    @Test
    void getLowStockByWarehouse_ShouldReturnLowStockEntriesForSpecificWarehouse() {
        List<ProductWarehouseResponse> warehouseLowStock = Collections.singletonList(lowStockEntry);
        when(productWarehouseService.getLowStockByWarehouse(100L))
                .thenReturn(ResponseEntity.ok(warehouseLowStock));

        ResponseEntity<List<ProductWarehouseResponse>> response =
                productWarehouseController.getLowStockByWarehouse(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getBelowRol());
        assertEquals(100L, response.getBody().get(0).getWarehouseId());
        verify(productWarehouseService, times(1)).getLowStockByWarehouse(100L);
    }

    @Test
    void getLowStockByWarehouse_ShouldReturnEmptyList_WhenNoLowStockAtWarehouse() {
        when(productWarehouseService.getLowStockByWarehouse(200L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<ProductWarehouseResponse>> response =
                productWarehouseController.getLowStockByWarehouse(200L);

        assertTrue(response.getBody().isEmpty());
    }

    // ─── updateThresholds ─────────────────────────────────────────────────────

    @Test
    void updateThresholds_ShouldReturnUpdatedEntry_WhenValidRequest() {
        ProductWarehouseResponse updatedEntry = ProductWarehouseResponse.builder()
                .id(1L).productId(5L).maxStock(600).rolPercent(35.0).stock(300)
                .currentStockPercent(50.0).belowRol(false).build();

        when(productWarehouseService.updateThresholds(eq(1L), any(UpdateProductWarehouseRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedEntry));

        ResponseEntity<ProductWarehouseResponse> response =
                productWarehouseController.updateThresholds(1L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(600, response.getBody().getMaxStock());
        assertEquals(35.0, response.getBody().getRolPercent());
        verify(productWarehouseService, times(1)).updateThresholds(eq(1L), any(UpdateProductWarehouseRequest.class));
    }

    @Test
    void updateThresholds_ShouldReturnNotFound_WhenEntryDoesNotExist() {
        when(productWarehouseService.updateThresholds(eq(999L), any(UpdateProductWarehouseRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<ProductWarehouseResponse> response =
                productWarehouseController.updateThresholds(999L, updateRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── restock ──────────────────────────────────────────────────────────────

    @Test
    void restock_ShouldReturnUpdatedEntry_WhenRestockSucceeds() {
        ProductWarehouseResponse restockedEntry = ProductWarehouseResponse.builder()
                .id(1L).productId(5L).stock(400).maxStock(500)
                .currentStockPercent(80.0).belowRol(false).build();

        when(productWarehouseService.restock(eq(1L), any(RestockRequest.class)))
                .thenReturn(ResponseEntity.ok(restockedEntry));

        ResponseEntity<ProductWarehouseResponse> response =
                productWarehouseController.restock(1L, restockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(400, response.getBody().getStock());
        assertFalse(response.getBody().getBelowRol());
        verify(productWarehouseService, times(1)).restock(eq(1L), any(RestockRequest.class));
    }

    @Test
    void restock_ShouldReturnBadRequest_WhenRestockExceedsMaxStock() {
        when(productWarehouseService.restock(eq(1L), any(RestockRequest.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        ResponseEntity<ProductWarehouseResponse> response =
                productWarehouseController.restock(1L, restockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void restock_ShouldPassCorrectIdAndRequestToService() {
        when(productWarehouseService.restock(1L, restockRequest))
                .thenReturn(ResponseEntity.ok(normalStockEntry));

        productWarehouseController.restock(1L, restockRequest);

        verify(productWarehouseService).restock(1L, restockRequest);
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenEntryDeletedSuccessfully() {
        when(productWarehouseService.delete(1L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> response = productWarehouseController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productWarehouseService, times(1)).delete(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenEntryDoesNotExist() {
        when(productWarehouseService.delete(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Void> response = productWarehouseController.delete(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void delete_ShouldCallDeleteOnlyOnce() {
        when(productWarehouseService.delete(anyLong())).thenReturn(ResponseEntity.noContent().build());

        productWarehouseController.delete(1L);

        verify(productWarehouseService, times(1)).delete(1L);
        verifyNoMoreInteractions(productWarehouseService);
    }
}