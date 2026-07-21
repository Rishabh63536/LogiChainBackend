package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.ProductWarehouseResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.mapper.ProductWarehouseMapper;
import com.cts.logichain360.repository.*;
import com.cts.logichain360.service.impl.ProductWarehouseServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductWarehouseServiceImplTest {

    @Mock private ProductWarehouseRepository pwRepo;
    @Mock private ProductRepository productRepo;
    @Mock private WarehouseRepository warehouseRepo;
    @Mock private ProductWarehouseMapper productWarehouseMapper;

    @InjectMocks private ProductWarehouseServiceImpl pwService;

    private Vendor mockVendor;
    private Product mockProduct;
    private Warehouse mockWarehouse;
    private ProductWarehouse mockPW;
    private ProductWarehouse mockLowPW;
    private ProductWarehouseResponse mockResponse;
    private ProductWarehouseResponse mockLowResponse;

    @BeforeEach
    void setUp() {
        User vendorUser = User.builder().id(30L).name("Sony India").phone("9999999999").build();
        mockVendor = Vendor.builder().id(30L).companyName("Sony India Pvt Ltd")
                .user(vendorUser).build();

        mockProduct = Product.builder().productId(5L)
                .productName("Sony WH-1000XM5").productPrice(29990.0)
                .vendor(mockVendor).build();

        mockWarehouse = Warehouse.builder().id(100L)
                .warehouseCode("WH-CHN-01").location("Chennai").capacity(5000).build();

        // Normal stock - above ROL
        mockPW = ProductWarehouse.builder().id(1L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(300).maxStock(500).rolPercent(40.0).build();

        // Low stock - below ROL
        mockLowPW = ProductWarehouse.builder().id(2L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(90).maxStock(500).rolPercent(40.0).build();

        mockResponse = ProductWarehouseResponse.builder()
                .id(1L).productId(5L).productName("Sony WH-1000XM5")
                .productPrice(29990.0).vendorId(30L).vendorCompanyName("Sony India Pvt Ltd")
                .warehouseId(100L).warehouseCode("WH-CHN-01").warehouseLocation("Chennai")
                .stock(300).maxStock(500).rolPercent(40.0)
                .currentStockPercent(60.0).belowRol(false).build();

        mockLowResponse = ProductWarehouseResponse.builder()
                .id(2L).productId(5L).productName("Sony WH-1000XM5")
                .stock(90).maxStock(500).rolPercent(40.0)
                .currentStockPercent(18.0).belowRol(true).build();
    }

    // ─── launch ───────────────────────────────────────────────────────────────

    @Test
    void launch_ShouldReturnCreated_WhenValidRequest() {
        LaunchProductAtWarehouseRequest req = LaunchProductAtWarehouseRequest.builder()
                .productId(5L).warehouseId(100L).stock(300).maxStock(500).rolPercent(40.0).build();

        when(productRepo.findById(5L)).thenReturn(Optional.of(mockProduct));
        when(warehouseRepo.findById(100L)).thenReturn(Optional.of(mockWarehouse));
        when(pwRepo.existsByProduct_ProductId(5L)).thenReturn(false);
        when(pwRepo.save(any(ProductWarehouse.class))).thenReturn(mockPW);
        when(productWarehouseMapper.toResponse(mockPW)).thenReturn(mockResponse);

        ResponseEntity<ProductWarehouseResponse> response = pwService.launch(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals(300, response.getBody().getStock());
        verify(pwRepo).save(any(ProductWarehouse.class));
    }

    @Test
    void launch_ShouldThrowException_WhenProductNotFound() {
        LaunchProductAtWarehouseRequest req = LaunchProductAtWarehouseRequest.builder()
                .productId(999L).warehouseId(100L).stock(100).maxStock(500).rolPercent(40.0).build();

        when(productRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pwService.launch(req));
        verify(pwRepo, never()).save(any());
    }

    @Test
    void launch_ShouldThrowException_WhenWarehouseNotFound() {
        LaunchProductAtWarehouseRequest req = LaunchProductAtWarehouseRequest.builder()
                .productId(5L).warehouseId(999L).stock(100).maxStock(500).rolPercent(40.0).build();

        when(productRepo.findById(5L)).thenReturn(Optional.of(mockProduct));
        when(warehouseRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pwService.launch(req));
    }

    @Test
    void launch_ShouldThrowException_WhenProductAlreadyLaunched() {
        LaunchProductAtWarehouseRequest req = LaunchProductAtWarehouseRequest.builder()
                .productId(5L).warehouseId(100L).stock(100).maxStock(500).rolPercent(40.0).build();

        when(productRepo.findById(5L)).thenReturn(Optional.of(mockProduct));
        when(warehouseRepo.findById(100L)).thenReturn(Optional.of(mockWarehouse));
        when(pwRepo.existsByProduct_ProductId(5L)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> pwService.launch(req));
    }

    @Test
    void launch_ShouldThrowException_WhenStockExceedsMaxStock() {
        LaunchProductAtWarehouseRequest req = LaunchProductAtWarehouseRequest.builder()
                .productId(5L).warehouseId(100L).stock(600).maxStock(500).rolPercent(40.0).build();

        when(productRepo.findById(5L)).thenReturn(Optional.of(mockProduct));
        when(warehouseRepo.findById(100L)).thenReturn(Optional.of(mockWarehouse));
        when(pwRepo.existsByProduct_ProductId(5L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> pwService.launch(req));
        verify(pwRepo, never()).save(any());
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnEntry_WhenExists() {
        when(pwRepo.findById(1L)).thenReturn(Optional.of(mockPW));
        when(productWarehouseMapper.toResponse(mockPW)).thenReturn(mockResponse);

        ResponseEntity<ProductWarehouseResponse> response = pwService.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertFalse(response.getBody().getBelowRol());
    }

    @Test
    void getById_ShouldThrowException_WhenNotExists() {
        when(pwRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pwService.getById(999L));
    }

    // ─── getByProductId ───────────────────────────────────────────────────────

    @Test
    void getByProductId_ShouldReturnEntry_WhenProductExists() {
        when(pwRepo.findByProduct_ProductId(5L)).thenReturn(Optional.of(mockPW));
        when(productWarehouseMapper.toResponse(mockPW)).thenReturn(mockResponse);

        ResponseEntity<ProductWarehouseResponse> response = pwService.getByProductId(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().getProductId());
    }

    @Test
    void getByProductId_ShouldThrowException_WhenProductNotLaunched() {
        when(pwRepo.findByProduct_ProductId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pwService.getByProductId(999L));
    }

    // ─── getByWarehouseId ─────────────────────────────────────────────────────

    @Test
    void getByWarehouseId_ShouldReturnEntries_WhenWarehouseExists() {
        when(warehouseRepo.existsById(100L)).thenReturn(true);
        when(pwRepo.findAllByWarehouse_Id(100L)).thenReturn(Arrays.asList(mockPW, mockLowPW));
        when(productWarehouseMapper.toResponse(mockPW)).thenReturn(mockResponse);
        when(productWarehouseMapper.toResponse(mockLowPW)).thenReturn(mockLowResponse);

        ResponseEntity<List<ProductWarehouseResponse>> response = pwService.getByWarehouseId(100L);

        assertEquals(2, response.getBody().size());
    }

    @Test
    void getByWarehouseId_ShouldThrowException_WhenWarehouseNotFound() {
        when(warehouseRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pwService.getByWarehouseId(999L));
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllEntries() {
        when(pwRepo.findAll()).thenReturn(Arrays.asList(mockPW, mockLowPW));
        when(productWarehouseMapper.toResponse(mockPW)).thenReturn(mockResponse);
        when(productWarehouseMapper.toResponse(mockLowPW)).thenReturn(mockLowResponse);

        ResponseEntity<List<ProductWarehouseResponse>> response = pwService.getAll();

        assertEquals(2, response.getBody().size());
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNone() {
        when(pwRepo.findAll()).thenReturn(List.of());

        assertTrue(pwService.getAll().getBody().isEmpty());
    }

    // ─── getLowStock ──────────────────────────────────────────────────────────

    @Test
    void getLowStock_ShouldReturnOnlyBelowRolEntries() {
        when(pwRepo.findAllBelowRol()).thenReturn(List.of(mockLowPW));
        when(productWarehouseMapper.toResponse(mockLowPW)).thenReturn(mockLowResponse);

        ResponseEntity<List<ProductWarehouseResponse>> response = pwService.getLowStock();

        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getBelowRol());
    }

    @Test
    void getLowStock_ShouldReturnEmpty_WhenNoBelowRol() {
        when(pwRepo.findAllBelowRol()).thenReturn(List.of());

        assertTrue(pwService.getLowStock().getBody().isEmpty());
    }

    // ─── getLowStockByWarehouse ───────────────────────────────────────────────

    @Test
    void getLowStockByWarehouse_ShouldReturnBelowRolEntriesForWarehouse() {
        when(warehouseRepo.existsById(100L)).thenReturn(true);
        when(pwRepo.findAllBelowRolByWarehouse(100L)).thenReturn(List.of(mockLowPW));
        when(productWarehouseMapper.toResponse(mockLowPW)).thenReturn(mockLowResponse);

        ResponseEntity<List<ProductWarehouseResponse>> response =
                pwService.getLowStockByWarehouse(100L);

        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getBelowRol());
    }

    @Test
    void getLowStockByWarehouse_ShouldThrowException_WhenWarehouseNotFound() {
        when(warehouseRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> pwService.getLowStockByWarehouse(999L));
    }

    @Test
    void getLowStockByWarehouse_ShouldReturnEmpty_WhenNoneBelow() {
        when(warehouseRepo.existsById(100L)).thenReturn(true);
        when(pwRepo.findAllBelowRolByWarehouse(100L)).thenReturn(List.of());

        assertTrue(pwService.getLowStockByWarehouse(100L).getBody().isEmpty());
    }

    // ─── updateThresholds ─────────────────────────────────────────────────────

    @Test
    void updateThresholds_ShouldUpdateMaxStockAndRol_WhenValid() {
        UpdateProductWarehouseRequest req = UpdateProductWarehouseRequest.builder()
                .maxStock(600).rolPercent(35.0).build();

        when(pwRepo.findById(1L)).thenReturn(Optional.of(mockPW));
        when(pwRepo.save(any())).thenReturn(mockPW);
        when(productWarehouseMapper.toResponse(mockPW)).thenReturn(mockResponse);

        ResponseEntity<ProductWarehouseResponse> response = pwService.updateThresholds(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(600, mockPW.getMaxStock());
        assertEquals(35.0, mockPW.getRolPercent());
        verify(pwRepo).save(mockPW);
    }

    @Test
    void updateThresholds_ShouldThrowException_WhenNewMaxStockBelowCurrentStock() {
        // current stock=300, trying to set maxStock=100 → invalid
        UpdateProductWarehouseRequest req = UpdateProductWarehouseRequest.builder()
                .maxStock(100).build();

        when(pwRepo.findById(1L)).thenReturn(Optional.of(mockPW));

        assertThrows(IllegalArgumentException.class,
                () -> pwService.updateThresholds(1L, req));
        verify(pwRepo, never()).save(any());
    }

    @Test
    void updateThresholds_ShouldThrowException_WhenEntryNotFound() {
        when(pwRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> pwService.updateThresholds(999L, UpdateProductWarehouseRequest.builder().build()));
    }

    // ─── restock ──────────────────────────────────────────────────────────────

    @Test
    void restock_ShouldIncreaseStock_WhenWithinMaxStock() {
        RestockRequest req = RestockRequest.builder().amount(100).build();

        when(pwRepo.findById(1L)).thenReturn(Optional.of(mockPW));
        when(pwRepo.save(any())).thenReturn(mockPW);
        when(productWarehouseMapper.toResponse(mockPW)).thenReturn(mockResponse);

        ResponseEntity<ProductWarehouseResponse> response = pwService.restock(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(400, mockPW.getStock()); // 300 + 100
        verify(pwRepo).save(mockPW);
    }

    @Test
    void restock_ShouldThrowException_WhenRestockExceedsMaxStock() {
        RestockRequest req = RestockRequest.builder().amount(300).build(); // 300+300=600 > maxStock 500

        when(pwRepo.findById(1L)).thenReturn(Optional.of(mockPW));

        assertThrows(IllegalArgumentException.class,
                () -> pwService.restock(1L, req));
        verify(pwRepo, never()).save(any());
    }

    @Test
    void restock_ShouldThrowException_WhenEntryNotFound() {
        when(pwRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> pwService.restock(999L, RestockRequest.builder().amount(50).build()));
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenEntryExists() {
        when(pwRepo.findById(1L)).thenReturn(Optional.of(mockPW));

        ResponseEntity<Void> response = pwService.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(pwRepo).delete(mockPW);
    }

    @Test
    void delete_ShouldThrowException_WhenEntryNotFound() {
        when(pwRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pwService.delete(999L));
        verify(pwRepo, never()).delete(any());
    }
}