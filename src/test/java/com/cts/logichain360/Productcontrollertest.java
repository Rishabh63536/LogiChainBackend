package com.cts.logichain360;

import com.cts.logichain360.controller.ProductController;
import com.cts.logichain360.dto.request.CreateProductRequest;
import com.cts.logichain360.dto.request.UpdateProductRequest;
import com.cts.logichain360.dto.response.ProductResponse;
import com.cts.logichain360.service.ProductService;
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
 * Unit tests for ProductController using Mockito.
 * Covers: create, getById, getAll, getByVendor, update, delete
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductResponse productResponse;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .productId(1L)
                .productName("Sony WH-1000XM5 Headphones")
                .productPrice(29990.0)
                .productDescription("Wireless headphones with noise cancellation")
                .vendorId(30L)
                .vendorCompanyName("Sony India Pvt Ltd")
                .build();

        createRequest = CreateProductRequest.builder()
                .productName("Sony WH-1000XM5 Headphones")
                .productPrice(29990.0)
                .productDescription("Wireless headphones with noise cancellation")
                .vendorId(30L)
                .build();

        updateRequest = UpdateProductRequest.builder()
                .productName("Sony WH-1000XM5 Headphones v2")
                .productPrice(27990.0)
                .build();
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    void create_ShouldReturnCreatedProduct_WhenValidRequest() {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(productResponse));

        ResponseEntity<ProductResponse> response = productController.create(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sony WH-1000XM5 Headphones", response.getBody().getProductName());
        assertEquals(29990.0, response.getBody().getProductPrice());
        assertEquals(30L, response.getBody().getVendorId());
        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void create_ShouldReturnConflict_WhenVendorAlreadyHasProductWithSameName() {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        ResponseEntity<ProductResponse> response = productController.create(createRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void create_ShouldReturnNotFound_WhenVendorDoesNotExist() {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<ProductResponse> response = productController.create(createRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void create_ShouldDelegateCorrectRequestToService() {
        when(productService.createProduct(createRequest))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(productResponse));

        productController.create(createRequest);

        verify(productService).createProduct(createRequest);
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnProduct_WhenProductExists() {
        when(productService.getProductById(1L)).thenReturn(ResponseEntity.ok(productResponse));

        ResponseEntity<ProductResponse> response = productController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getProductId());
        assertEquals("Sony WH-1000XM5 Headphones", response.getBody().getProductName());
        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenProductDoesNotExist() {
        when(productService.getProductById(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<ProductResponse> response = productController.getById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllProducts_WhenProductsExist() {
        ProductResponse product2 = ProductResponse.builder()
                .productId(2L).productName("LG 27UK850 Monitor")
                .productPrice(35000.0).vendorId(31L).vendorCompanyName("LG India").build();

        List<ProductResponse> products = Arrays.asList(productResponse, product2);
        when(productService.getAllProducts()).thenReturn(ResponseEntity.ok(products));

        ResponseEntity<List<ProductResponse>> response = productController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoProductsExist() {
        when(productService.getAllProducts()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<ProductResponse>> response = productController.getAll();

        assertTrue(response.getBody().isEmpty());
    }

    // ─── getByVendor ──────────────────────────────────────────────────────────

    @Test
    void getByVendor_ShouldReturnProductsBelongingToVendor() {
        ProductResponse product2 = ProductResponse.builder()
                .productId(2L).productName("Sony Earbuds").productPrice(9990.0)
                .vendorId(30L).vendorCompanyName("Sony India Pvt Ltd").build();

        List<ProductResponse> vendorProducts = Arrays.asList(productResponse, product2);
        when(productService.getProductsByVendor(30L)).thenReturn(ResponseEntity.ok(vendorProducts));

        ResponseEntity<List<ProductResponse>> response = productController.getByVendor(30L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        response.getBody().forEach(p -> assertEquals(30L, p.getVendorId()));
        verify(productService, times(1)).getProductsByVendor(30L);
    }

    @Test
    void getByVendor_ShouldReturnEmptyList_WhenVendorHasNoProducts() {
        when(productService.getProductsByVendor(99L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<ProductResponse>> response = productController.getByVendor(99L);

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getByVendor_ShouldNotCallGetAllProducts() {
        when(productService.getProductsByVendor(30L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        productController.getByVendor(30L);

        verify(productService).getProductsByVendor(30L);
        verify(productService, never()).getAllProducts();
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    void update_ShouldReturnUpdatedProduct_WhenValidRequest() {
        ProductResponse updatedProduct = ProductResponse.builder()
                .productId(1L).productName("Sony WH-1000XM5 Headphones v2")
                .productPrice(27990.0).vendorId(30L).vendorCompanyName("Sony India Pvt Ltd").build();

        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedProduct));

        ResponseEntity<ProductResponse> response = productController.update(1L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sony WH-1000XM5 Headphones v2", response.getBody().getProductName());
        assertEquals(27990.0, response.getBody().getProductPrice());
        verify(productService, times(1)).updateProduct(eq(1L), any(UpdateProductRequest.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenProductDoesNotExist() {
        when(productService.updateProduct(eq(999L), any(UpdateProductRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<ProductResponse> response = productController.update(999L, updateRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void update_ShouldPassCorrectIdAndRequestToService() {
        when(productService.updateProduct(1L, updateRequest))
                .thenReturn(ResponseEntity.ok(productResponse));

        productController.update(1L, updateRequest);

        verify(productService).updateProduct(1L, updateRequest);
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenProductDeletedSuccessfully() {
        when(productService.deleteProduct(1L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> response = productController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenProductDoesNotExist() {
        when(productService.deleteProduct(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Void> response = productController.delete(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void delete_ShouldCallDeleteOnlyOnce() {
        when(productService.deleteProduct(anyLong())).thenReturn(ResponseEntity.noContent().build());

        productController.delete(1L);

        verify(productService, times(1)).deleteProduct(1L);
        verifyNoMoreInteractions(productService);
    }
}