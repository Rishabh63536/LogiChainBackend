package com.cts.logichain360;

import com.cts.logichain360.controller.DriverController;
import com.cts.logichain360.dto.request.DriverAvailabilityRequest;
import com.cts.logichain360.dto.request.UpdateDriverRequest;
import com.cts.logichain360.dto.response.DriverResponse;
import com.cts.logichain360.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DriverController using Mockito.
 * Covers: getById, getByUserId, getAll, getAvailable, update, updateAvailability, delete
 */
@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    // ─── Test Data ────────────────────────────────────────────────────────────

    private DriverResponse availableDriver;
    private DriverResponse unavailableDriver;
    private UpdateDriverRequest updateRequest;
    private DriverAvailabilityRequest availabilityRequest;

    @BeforeEach
    void setUp() {
        availableDriver = DriverResponse.builder()
                .id(1L)
                .userId(20L)
                .userName("Ravi Kumar")
                .userPhone("9876543210")
                .licenseNumber("TN1234567890")
                .licenseExpiry(LocalDate.of(2027, 12, 31))
                .available(true)
                .build();

        unavailableDriver = DriverResponse.builder()
                .id(2L)
                .userId(21L)
                .userName("Suresh Raj")
                .userPhone("9876543211")
                .licenseNumber("TN0987654321")
                .licenseExpiry(LocalDate.of(2026, 6, 30))
                .available(false)
                .build();

        updateRequest = UpdateDriverRequest.builder()
                .licenseNumber("TN1234567891")
                .licenseExpiry(LocalDate.of(2028, 12, 31))
                .build();

        availabilityRequest = DriverAvailabilityRequest.builder()
                .available(false)
                .build();
    }

    // ─── GetById Tests ────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnDriver_WhenDriverExists() {
        // Arrange
        when(driverService.getDriverById(1L)).thenReturn(ResponseEntity.ok(availableDriver));

        // Act
        ResponseEntity<DriverResponse> response = driverController.getById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Ravi Kumar", response.getBody().getUserName());
        assertTrue(response.getBody().getAvailable());

        verify(driverService, times(1)).getDriverById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenDriverDoesNotExist() {
        // Arrange
        when(driverService.getDriverById(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<DriverResponse> response = driverController.getById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ─── GetByUserId Tests ────────────────────────────────────────────────────

    @Test
    void getByUserId_ShouldReturnDriver_WhenUserIdExists() {
        // Arrange
        when(driverService.getDriverByUserId(20L)).thenReturn(ResponseEntity.ok(availableDriver));

        // Act
        ResponseEntity<DriverResponse> response = driverController.getByUserId(20L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(20L, response.getBody().getUserId());

        verify(driverService, times(1)).getDriverByUserId(20L);
    }

    @Test
    void getByUserId_ShouldReturnNotFound_WhenUserIdDoesNotExist() {
        // Arrange
        when(driverService.getDriverByUserId(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<DriverResponse> response = driverController.getByUserId(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── GetAll Tests ─────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllDrivers_WhenDriversExist() {
        // Arrange
        List<DriverResponse> drivers = Arrays.asList(availableDriver, unavailableDriver);
        when(driverService.getAllDrivers()).thenReturn(ResponseEntity.ok(drivers));

        // Act
        ResponseEntity<List<DriverResponse>> response = driverController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(driverService, times(1)).getAllDrivers();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoDriversExist() {
        // Arrange
        when(driverService.getAllDrivers()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        ResponseEntity<List<DriverResponse>> response = driverController.getAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ─── GetAvailable Tests ───────────────────────────────────────────────────

    @Test
    void getAvailable_ShouldReturnOnlyAvailableDrivers() {
        // Arrange
        List<DriverResponse> availableDrivers = Collections.singletonList(availableDriver);
        when(driverService.getAvailableDrivers()).thenReturn(ResponseEntity.ok(availableDrivers));

        // Act
        ResponseEntity<List<DriverResponse>> response = driverController.getAvailable();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getAvailable());

        verify(driverService, times(1)).getAvailableDrivers();
    }

    @Test
    void getAvailable_ShouldReturnEmptyList_WhenNoDriversAvailable() {
        // Arrange
        when(driverService.getAvailableDrivers()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        ResponseEntity<List<DriverResponse>> response = driverController.getAvailable();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAvailable_ShouldNotCallGetAllDrivers() {
        // Arrange
        when(driverService.getAvailableDrivers()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        driverController.getAvailable();

        // Assert
        verify(driverService).getAvailableDrivers();
        verify(driverService, never()).getAllDrivers();
    }

    // ─── Update Tests ─────────────────────────────────────────────────────────

    @Test
    void update_ShouldReturnUpdatedDriver_WhenValidRequest() {
        // Arrange
        DriverResponse updatedDriver = DriverResponse.builder()
                .id(1L).userId(20L).userName("Ravi Kumar")
                .licenseNumber("TN1234567891")
                .licenseExpiry(LocalDate.of(2028, 12, 31))
                .available(true)
                .build();

        when(driverService.updateDriver(eq(1L), any(UpdateDriverRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedDriver));

        // Act
        ResponseEntity<DriverResponse> response = driverController.update(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TN1234567891", response.getBody().getLicenseNumber());

        verify(driverService, times(1)).updateDriver(eq(1L), any(UpdateDriverRequest.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenDriverDoesNotExist() {
        // Arrange
        when(driverService.updateDriver(eq(999L), any(UpdateDriverRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<DriverResponse> response = driverController.update(999L, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── UpdateAvailability Tests ─────────────────────────────────────────────

    @Test
    void updateAvailability_ShouldMarkDriverAsUnavailable_WhenRequested() {
        // Arrange
        DriverResponse updatedDriver = DriverResponse.builder()
                .id(1L).userId(20L).userName("Ravi Kumar")
                .available(false).build();

        when(driverService.updateAvailability(eq(1L), any(DriverAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedDriver));

        // Act
        ResponseEntity<DriverResponse> response = driverController.updateAvailability(1L, availabilityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getAvailable());

        verify(driverService, times(1)).updateAvailability(eq(1L), any(DriverAvailabilityRequest.class));
    }

    @Test
    void updateAvailability_ShouldMarkDriverAsAvailable_WhenRequested() {
        // Arrange
        DriverAvailabilityRequest makeAvailable = DriverAvailabilityRequest.builder().available(true).build();
        DriverResponse updatedDriver = DriverResponse.builder()
                .id(2L).available(true).build();

        when(driverService.updateAvailability(eq(2L), any(DriverAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedDriver));

        // Act
        ResponseEntity<DriverResponse> response = driverController.updateAvailability(2L, makeAvailable);

        // Assert
        assertTrue(response.getBody().getAvailable());
        verify(driverService).updateAvailability(eq(2L), any(DriverAvailabilityRequest.class));
    }

    @Test
    void updateAvailability_ShouldPassCorrectIdAndRequest() {
        // Arrange
        when(driverService.updateAvailability(1L, availabilityRequest))
                .thenReturn(ResponseEntity.ok(unavailableDriver));

        // Act
        driverController.updateAvailability(1L, availabilityRequest);

        // Assert
        verify(driverService).updateAvailability(1L, availabilityRequest);
    }

    // ─── Delete Tests ─────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenDriverDeletedSuccessfully() {
        // Arrange
        when(driverService.deleteDriver(1L)).thenReturn(ResponseEntity.noContent().build());

        // Act
        ResponseEntity<Void> response = driverController.delete(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(driverService, times(1)).deleteDriver(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenDriverDoesNotExist() {
        // Arrange
        when(driverService.deleteDriver(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<Void> response = driverController.delete(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}