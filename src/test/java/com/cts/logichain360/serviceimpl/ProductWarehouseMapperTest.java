package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.response.ProductWarehouseResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.mapper.ProductWarehouseMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductWarehouseMapper.
 * No mocking needed — mapper is a pure function.
 */
class ProductWarehouseMapperTest {

    private ProductWarehouseMapper mapper;
    private Vendor mockVendor;
    private Product mockProduct;
    private Warehouse mockWarehouse;

    @BeforeEach
    void setUp() {
        mapper = new ProductWarehouseMapper();

        User vendorUser = User.builder().id(30L).name("Sony India").phone("9999999999").build();
        mockVendor = Vendor.builder().id(30L).companyName("Sony India Pvt Ltd")
                .user(vendorUser).build();

        mockProduct = Product.builder().productId(5L)
                .productName("Sony WH-1000XM5").productPrice(29990.0)
                .vendor(mockVendor).build();

        mockWarehouse = Warehouse.builder().id(100L)
                .warehouseCode("WH-CHN-01").location("Chennai").capacity(5000).build();
    }

    @Test
    void toResponse_ShouldMapAllFields_WhenStockAboveRol() {
        ProductWarehouse pw = ProductWarehouse.builder().id(1L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(300).maxStock(500).rolPercent(40.0).build();

        ProductWarehouseResponse response = mapper.toResponse(pw);

        assertEquals(1L, response.getId());
        assertEquals(5L, response.getProductId());
        assertEquals("Sony WH-1000XM5", response.getProductName());
        assertEquals(29990.0, response.getProductPrice());
        assertEquals(30L, response.getVendorId());
        assertEquals("Sony India Pvt Ltd", response.getVendorCompanyName());
        assertEquals(100L, response.getWarehouseId());
        assertEquals("WH-CHN-01", response.getWarehouseCode());
        assertEquals("Chennai", response.getWarehouseLocation());
        assertEquals(300, response.getStock());
        assertEquals(500, response.getMaxStock());
        assertEquals(40.0, response.getRolPercent());
        assertEquals(60.0, response.getCurrentStockPercent());
        assertFalse(response.getBelowRol());
    }

    @Test
    void toResponse_ShouldReturnBelowRolTrue_WhenStockBelowRol() {
        // 90/500 = 18% which is below rolPercent=40%
        ProductWarehouse pw = ProductWarehouse.builder().id(2L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(90).maxStock(500).rolPercent(40.0).build();

        ProductWarehouseResponse response = mapper.toResponse(pw);

        assertTrue(response.getBelowRol());
        assertEquals(18.0, response.getCurrentStockPercent());
    }

    @Test
    void toResponse_ShouldReturnZeroPercent_WhenMaxStockIsZero() {
        ProductWarehouse pw = ProductWarehouse.builder().id(3L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(0).maxStock(0).rolPercent(40.0).build();

        ProductWarehouseResponse response = mapper.toResponse(pw);

        assertEquals(0.0, response.getCurrentStockPercent());
    }

    @Test
    void toResponse_ShouldReturnZeroPercent_WhenMaxStockIsNull() {
        ProductWarehouse pw = ProductWarehouse.builder().id(4L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(100).maxStock(null).rolPercent(40.0).build();

        ProductWarehouseResponse response = mapper.toResponse(pw);

        assertEquals(0.0, response.getCurrentStockPercent());
    }

    @Test
    void toResponse_ShouldRoundCurrentStockPercent_ToTwoDecimalPlaces() {
        // 100/300 = 33.333...% should round to 33.33
        ProductWarehouse pw = ProductWarehouse.builder().id(5L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(100).maxStock(300).rolPercent(40.0).build();

        ProductWarehouseResponse response = mapper.toResponse(pw);

        assertEquals(33.33, response.getCurrentStockPercent());
    }

    @Test
    void toResponse_ShouldReturnBelowRolFalse_WhenStockExactlyAtRolThreshold() {
        // 200/500 = 40% which equals rolPercent=40% — NOT below (needs to be strictly less)
        ProductWarehouse pw = ProductWarehouse.builder().id(6L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(200).maxStock(500).rolPercent(40.0).build();

        ProductWarehouseResponse response = mapper.toResponse(pw);

        assertFalse(response.getBelowRol()); // exactly at threshold, not below
    }
}