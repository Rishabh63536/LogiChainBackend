package com.cts.logichain360;

import com.cts.logichain360.controller.WarehouseManagerController;
import com.cts.logichain360.dto.request.UpdateWarehouseManagerRequest;
import com.cts.logichain360.dto.request.WarehouseAssignmentRequest;
import com.cts.logichain360.dto.response.WarehouseManagerResponse;
import com.cts.logichain360.service.WarehouseManagerService;
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
 * Unit tests for WarehouseManagerController using Mockito.
 * Covers: getById, getByUserId, getAll, update, assignWarehouse, delete
 */
@ExtendWith(MockitoExtension.class)
class WarehouseManagerControllerTest {

    @Mock
    private WarehouseManagerService wmService;

    @InjectMocks
    private WarehouseManagerController warehouseManagerController;

    // ─── Test Data ────────────────────────────────────────────────────────────

    private WarehouseManagerResponse wmResponse;
    private UpdateWarehouseManagerRequest updateRequest;
    private WarehouseAssignmentRequest assignmentRequest;

    @BeforeEach
    void setUp() {
        wmResponse = WarehouseManagerResponse.builder()
                .id(1L)
                .userId(40L)
                .userName("Mohan Kumar")
                .userPhone("9876543210")
                .employeeCode("EMP001")
                .designation("Senior Manager")
                .assignedWarehouseCode("WH-CHN-001")
                .assignedWarehouseLocation("Chennai")
                .build();

        updateRequest = UpdateWarehouseManagerRequest.builder()
                .designation("Lead Manager")
                .build();

        assignmentRequest = WarehouseAssignmentRequest.builder()
                .warehouseId(200L)
                .build();
    }

    // ─── GetById Tests ────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnWarehouseManager_WhenExists() {
        // Arrange
        when(wmService.getById(1L)).thenReturn(ResponseEntity.ok(wmResponse));

        // Act
        ResponseEntity<WarehouseManagerResponse> response = warehouseManagerController.getById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Mohan Kumar", response.getBody().getUserName());
        assertEquals("EMP001", response.getBody().getEmployeeCode());

        verify(wmService, times(1)).getById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenManagerDoesNotExist() {
        // Arrange
        when(wmService.getById(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<WarehouseManagerResponse> response = warehouseManagerController.getById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(wmService).getById(999L);
    }

    @Test
    void getById_ShouldReturnWarehouseDetails_WhenManagerHasAssignedWarehouse() {
        // Arrange
        when(wmService.getById(1L)).thenReturn(ResponseEntity.ok(wmResponse));

        // Act
        ResponseEntity<WarehouseManagerResponse> response = warehouseManagerController.getById(1L);

        // Assert
        assertEquals(100L, response.getBody().getAssignedWarehouseId());
        assertEquals("WH-CHN-001", response.getBody().getAssignedWarehouseCode());
        assertEquals("Chennai", response.getBody().getAssignedWarehouseLocation());
    }

    // ─── GetByUserId Tests ────────────────────────────────────────────────────

    @Test
    void getByUserId_ShouldReturnManager_WhenUserIdExists() {
        // Arrange
        when(wmService.getByUserId(40L)).thenReturn(ResponseEntity.ok(wmResponse));

        // Act
        ResponseEntity<WarehouseManagerResponse> response = warehouseManagerController.getByUserId(40L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(40L, response.getBody().getUserId());

        verify(wmService, times(1)).getByUserId(40L);
    }

    @Test
    void getByUserId_ShouldReturnNotFound_WhenUserIdDoesNotExist() {
        // Arrange
        when(wmService.getByUserId(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<WarehouseManagerResponse> response = warehouseManagerController.getByUserId(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── GetAll Tests ─────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllManagers_WhenManagersExist() {
        // Arrange
        WarehouseManagerResponse manager2 = WarehouseManagerResponse.builder()
                .id(2L).userId(41L).userName("Kavitha R").build();

        List<WarehouseManagerResponse> managers = Arrays.asList(wmResponse, manager2);
        when(wmService.getAll()).thenReturn(ResponseEntity.ok(managers));

        // Act
        ResponseEntity<List<WarehouseManagerResponse>> response = warehouseManagerController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Mohan Kumar", response.getBody().get(0).getUserName());
        assertEquals("Kavitha R", response.getBody().get(1).getUserName());

        verify(wmService, times(1)).getAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoManagersExist() {
        // Arrange
        when(wmService.getAll()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        ResponseEntity<List<WarehouseManagerResponse>> response = warehouseManagerController.getAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAll_ShouldCallServiceExactlyOnce() {
        // Arrange
        when(wmService.getAll()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        warehouseManagerController.getAll();

        // Assert
        verify(wmService, times(1)).getAll();
        verifyNoMoreInteractions(wmService);
    }

    // ─── Update Tests ─────────────────────────────────────────────────────────

    @Test
    void update_ShouldReturnUpdatedManager_WhenValidRequest() {
        // Arrange
        WarehouseManagerResponse updatedResponse = WarehouseManagerResponse.builder()
                .id(1L).userId(40L).userName("Mohan Kumar")
                .employeeCode("EMP001")
                .designation("Lead Manager")
                .build();

        when(wmService.update(eq(1L), any(UpdateWarehouseManagerRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedResponse));

        // Act
        ResponseEntity<WarehouseManagerResponse> response =
                warehouseManagerController.update(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Lead Manager", response.getBody().getDesignation());

        verify(wmService, times(1)).update(eq(1L), any(UpdateWarehouseManagerRequest.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenManagerDoesNotExist() {
        // Arrange
        when(wmService.update(eq(999L), any(UpdateWarehouseManagerRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<WarehouseManagerResponse> response =
                warehouseManagerController.update(999L, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void update_ShouldPassCorrectIdAndRequestToService() {
        // Arrange
        when(wmService.update(1L, updateRequest)).thenReturn(ResponseEntity.ok(wmResponse));

        // Act
        warehouseManagerController.update(1L, updateRequest);

        // Assert
        verify(wmService).update(1L, updateRequest);
    }

    // ─── AssignWarehouse Tests ────────────────────────────────────────────────

    @Test
    void assignWarehouse_ShouldReturnUpdatedManager_WhenWarehouseAssigned() {
        // Arrange
        WarehouseManagerResponse assignedResponse = WarehouseManagerResponse.builder()
                .id(1L).userId(40L).userName("Mohan Kumar")
                .assignedWarehouseId(200L)
                .assignedWarehouseCode("WH-CHN-002")
                .assignedWarehouseLocation("Coimbatore")
                .build();

        when(wmService.assignWarehouse(eq(1L), any(WarehouseAssignmentRequest.class)))
                .thenReturn(ResponseEntity.ok(assignedResponse));

        // Act
        ResponseEntity<WarehouseManagerResponse> response =
                warehouseManagerController.assignWarehouse(1L, assignmentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200L, response.getBody().getAssignedWarehouseId());
        assertEquals("WH-CHN-002", response.getBody().getAssignedWarehouseCode());

        verify(wmService, times(1)).assignWarehouse(eq(1L), any(WarehouseAssignmentRequest.class));
    }

    @Test
    void assignWarehouse_ShouldReturnNotFound_WhenManagerDoesNotExist() {
        // Arrange
        when(wmService.assignWarehouse(eq(999L), any(WarehouseAssignmentRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<WarehouseManagerResponse> response =
                warehouseManagerController.assignWarehouse(999L, assignmentRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void assignWarehouse_ShouldPassCorrectIdAndRequestToService() {
        // Arrange
        when(wmService.assignWarehouse(1L, assignmentRequest)).thenReturn(ResponseEntity.ok(wmResponse));

        // Act
        warehouseManagerController.assignWarehouse(1L, assignmentRequest);

        // Assert
        verify(wmService).assignWarehouse(1L, assignmentRequest);
    }

    @Test
    void assignWarehouse_ShouldNotCallUpdateMethod() {
        // Arrange
        when(wmService.assignWarehouse(eq(1L), any(WarehouseAssignmentRequest.class)))
                .thenReturn(ResponseEntity.ok(wmResponse));

        // Act
        warehouseManagerController.assignWarehouse(1L, assignmentRequest);

        // Assert
        verify(wmService, never()).update(anyLong(), any(UpdateWarehouseManagerRequest.class));
    }

    // ─── Delete Tests ─────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenManagerDeletedSuccessfully() {
        // Arrange
        when(wmService.delete(1L)).thenReturn(ResponseEntity.noContent().build());

        // Act
        ResponseEntity<Void> response = warehouseManagerController.delete(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(wmService, times(1)).delete(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenManagerDoesNotExist() {
        // Arrange
        when(wmService.delete(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<Void> response = warehouseManagerController.delete(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void delete_ShouldCallDeleteOnlyOnce() {
        // Arrange
        when(wmService.delete(anyLong())).thenReturn(ResponseEntity.noContent().build());

        // Act
        warehouseManagerController.delete(1L);

        // Assert
        verify(wmService, times(1)).delete(1L);
        verifyNoMoreInteractions(wmService);
    }
}