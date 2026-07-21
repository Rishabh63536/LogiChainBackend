package com.cts.logichain360;

import com.cts.logichain360.controller.VendorController;
import com.cts.logichain360.dto.request.UpdateVendorRequest;
import com.cts.logichain360.dto.response.VendorResponse;
import com.cts.logichain360.service.VendorService;
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
 * Unit tests for VendorController using Mockito.
 * Covers: getById, getByUserId, getAll, update, delete
 */
@ExtendWith(MockitoExtension.class)
class VendorControllerTest {

    @Mock
    private VendorService vendorService;

    @InjectMocks
    private VendorController vendorController;

    // ─── Test Data ────────────────────────────────────────────────────────────

    private VendorResponse vendorResponse;
    private UpdateVendorRequest updateRequest;

    @BeforeEach
    void setUp() {
        vendorResponse = VendorResponse.builder()
                .id(1L)
                .userId(30L)
                .userName("Priya Vendor")
                .userPhone("9876543210")
                .companyName("Priya Supplies Ltd")
                .gstNumber("22BBBBB0000B1Z6")
                .email("priya@supplies.com")
                .businessAddress("456 Industrial Area, Chennai")
                .contactPerson("Priya Sharma")
                .paymentTerms("Net 60")
                .build();

        updateRequest = UpdateVendorRequest.builder()
                .companyName("Priya Supplies Updated")
                .contactPerson("Raj Sharma")
                .paymentTerms("Net 30")
                .build();
    }

    // ─── GetById Tests ────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnVendor_WhenVendorExists() {
        // Arrange
        when(vendorService.getVendorById(1L)).thenReturn(ResponseEntity.ok(vendorResponse));

        // Act
        ResponseEntity<VendorResponse> response = vendorController.getById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Priya Vendor", response.getBody().getUserName());
        assertEquals("Priya Supplies Ltd", response.getBody().getCompanyName());

        verify(vendorService, times(1)).getVendorById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenVendorDoesNotExist() {
        // Arrange
        when(vendorService.getVendorById(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<VendorResponse> response = vendorController.getById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ─── GetByUserId Tests ────────────────────────────────────────────────────

    @Test
    void getByUserId_ShouldReturnVendor_WhenUserIdExists() {
        // Arrange
        when(vendorService.getVendorByUserId(30L)).thenReturn(ResponseEntity.ok(vendorResponse));

        // Act
        ResponseEntity<VendorResponse> response = vendorController.getByUserId(30L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(30L, response.getBody().getUserId());

        verify(vendorService, times(1)).getVendorByUserId(30L);
    }

    @Test
    void getByUserId_ShouldReturnNotFound_WhenUserIdDoesNotMapToVendor() {
        // Arrange
        when(vendorService.getVendorByUserId(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<VendorResponse> response = vendorController.getByUserId(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(vendorService).getVendorByUserId(999L);
    }

    // ─── GetAll Tests ─────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllVendors_WhenVendorsExist() {
        // Arrange
        VendorResponse vendor2 = VendorResponse.builder()
                .id(2L).userId(31L).userName("Kiran Vendor")
                .companyName("Kiran Traders").build();

        List<VendorResponse> vendors = Arrays.asList(vendorResponse, vendor2);
        when(vendorService.getAllVendors()).thenReturn(ResponseEntity.ok(vendors));

        // Act
        ResponseEntity<List<VendorResponse>> response = vendorController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(vendorService, times(1)).getAllVendors();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoVendorsExist() {
        // Arrange
        when(vendorService.getAllVendors()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        ResponseEntity<List<VendorResponse>> response = vendorController.getAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ─── Update Tests ─────────────────────────────────────────────────────────

    @Test
    void update_ShouldReturnUpdatedVendor_WhenValidRequest() {
        // Arrange
        VendorResponse updatedVendor = VendorResponse.builder()
                .id(1L).userId(30L).userName("Priya Vendor")
                .companyName("Priya Supplies Updated")
                .contactPerson("Raj Sharma")
                .paymentTerms("Net 30")
                .build();

        when(vendorService.updateVendor(eq(1L), any(UpdateVendorRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedVendor));

        // Act
        ResponseEntity<VendorResponse> response = vendorController.update(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Priya Supplies Updated", response.getBody().getCompanyName());
        assertEquals("Raj Sharma", response.getBody().getContactPerson());

        verify(vendorService, times(1)).updateVendor(eq(1L), any(UpdateVendorRequest.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenVendorDoesNotExist() {
        // Arrange
        when(vendorService.updateVendor(eq(999L), any(UpdateVendorRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<VendorResponse> response = vendorController.update(999L, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── Delete Tests ─────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenVendorDeletedSuccessfully() {
        // Arrange
        when(vendorService.deleteVendor(1L)).thenReturn(ResponseEntity.noContent().build());

        // Act
        ResponseEntity<Void> response = vendorController.delete(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(vendorService, times(1)).deleteVendor(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenVendorDoesNotExist() {
        // Arrange
        when(vendorService.deleteVendor(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<Void> response = vendorController.delete(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void delete_ShouldCallDeleteMethodOnlyOnce() {
        // Arrange
        when(vendorService.deleteVendor(anyLong())).thenReturn(ResponseEntity.noContent().build());

        // Act
        vendorController.delete(1L);

        // Assert
        verify(vendorService, times(1)).deleteVendor(1L);
        verifyNoMoreInteractions(vendorService);
    }
}