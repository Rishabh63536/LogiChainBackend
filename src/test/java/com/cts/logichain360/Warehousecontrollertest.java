package com.cts.logichain360;

import com.cts.logichain360.controller.WarehouseController;
import com.cts.logichain360.dto.request.CreateWarehouseRequest;
import com.cts.logichain360.dto.request.UpdateWarehouseRequest;
import com.cts.logichain360.dto.response.WarehouseResponse;
import com.cts.logichain360.service.WarehouseService;
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
 * Unit tests for WarehouseController using Mockito.
 * Covers: create, getById, getAll, update, delete
 */
@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseController warehouseController;

    private WarehouseResponse warehouseResponse;
    private WarehouseResponse warehouseWithNoManager;
    private CreateWarehouseRequest createRequest;
    private UpdateWarehouseRequest updateRequest;

    @BeforeEach
    void setUp() {
        warehouseResponse = WarehouseResponse.builder()
                .id(1L)
                .warehouseCode("WH-CHN-01")
                .location("Chennai, Tamil Nadu")
                .capacity(5000)
                .managerId(40L)
                .managerName("Mohan Kumar")
                .managerEmployeeCode("EMP001")
                .build();

        warehouseWithNoManager = WarehouseResponse.builder()
                .id(2L)
                .warehouseCode("WH-BLR-01")
                .location("Bangalore, Karnataka")
                .capacity(3000)
                .managerId(null)
                .managerName(null)
                .managerEmployeeCode(null)
                .build();

        createRequest = CreateWarehouseRequest.builder()
                .warehouseCode("WH-CHN-01")
                .location("Chennai, Tamil Nadu")
                .capacity(5000)
                .build();

        updateRequest = UpdateWarehouseRequest.builder()
                .location("Chennai, Tamil Nadu - Updated")
                .capacity(6000)
                .build();
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    void create_ShouldReturnCreatedWarehouse_WhenValidRequest() {
        when(warehouseService.createWarehouse(any(CreateWarehouseRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(warehouseResponse));

        ResponseEntity<WarehouseResponse> response = warehouseController.create(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("WH-CHN-01", response.getBody().getWarehouseCode());
        assertEquals("Chennai, Tamil Nadu", response.getBody().getLocation());
        assertEquals(5000, response.getBody().getCapacity());
        verify(warehouseService, times(1)).createWarehouse(any(CreateWarehouseRequest.class));
    }

    @Test
    void create_ShouldReturnConflict_WhenWarehouseCodeAlreadyExists() {
        when(warehouseService.createWarehouse(any(CreateWarehouseRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());

        ResponseEntity<WarehouseResponse> response = warehouseController.create(createRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void create_ShouldReturnOk_WhenServiceReturnsOk() {
        when(warehouseService.createWarehouse(any(CreateWarehouseRequest.class)))
                .thenReturn(ResponseEntity.ok(warehouseResponse));

        ResponseEntity<WarehouseResponse> response = warehouseController.create(createRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void create_ShouldDelegateCorrectRequestToService() {
        when(warehouseService.createWarehouse(createRequest))
                .thenReturn(ResponseEntity.ok(warehouseResponse));

        warehouseController.create(createRequest);

        verify(warehouseService).createWarehouse(createRequest);
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnWarehouse_WhenWarehouseExists() {
        when(warehouseService.getById(1L)).thenReturn(ResponseEntity.ok(warehouseResponse));

        ResponseEntity<WarehouseResponse> response = warehouseController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("WH-CHN-01", response.getBody().getWarehouseCode());
        verify(warehouseService, times(1)).getById(1L);
    }

    @Test
    void getById_ShouldReturnWarehouseWithManagerDetails_WhenManagerAssigned() {
        when(warehouseService.getById(1L)).thenReturn(ResponseEntity.ok(warehouseResponse));

        ResponseEntity<WarehouseResponse> response = warehouseController.getById(1L);

        assertNotNull(response.getBody().getManagerId());
        assertEquals("Mohan Kumar", response.getBody().getManagerName());
        assertEquals("EMP001", response.getBody().getManagerEmployeeCode());
    }

    @Test
    void getById_ShouldReturnWarehouseWithNullManagerFields_WhenNoManagerAssigned() {
        when(warehouseService.getById(2L)).thenReturn(ResponseEntity.ok(warehouseWithNoManager));

        ResponseEntity<WarehouseResponse> response = warehouseController.getById(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getManagerId());
        assertNull(response.getBody().getManagerName());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenWarehouseDoesNotExist() {
        when(warehouseService.getById(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<WarehouseResponse> response = warehouseController.getById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllWarehouses_WhenWarehousesExist() {
        List<WarehouseResponse> warehouses = Arrays.asList(warehouseResponse, warehouseWithNoManager);
        when(warehouseService.getAll()).thenReturn(ResponseEntity.ok(warehouses));

        ResponseEntity<List<WarehouseResponse>> response = warehouseController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("WH-CHN-01", response.getBody().get(0).getWarehouseCode());
        assertEquals("WH-BLR-01", response.getBody().get(1).getWarehouseCode());
        verify(warehouseService, times(1)).getAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoWarehousesExist() {
        when(warehouseService.getAll()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<WarehouseResponse>> response = warehouseController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAll_ShouldCallServiceExactlyOnce() {
        when(warehouseService.getAll()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        warehouseController.getAll();

        verify(warehouseService, times(1)).getAll();
        verifyNoMoreInteractions(warehouseService);
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    void update_ShouldReturnUpdatedWarehouse_WhenValidRequest() {
        WarehouseResponse updatedWarehouse = WarehouseResponse.builder()
                .id(1L).warehouseCode("WH-CHN-01")
                .location("Chennai, Tamil Nadu - Updated").capacity(6000)
                .managerId(40L).managerName("Mohan Kumar").managerEmployeeCode("EMP001")
                .build();

        when(warehouseService.update(eq(1L), any(UpdateWarehouseRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedWarehouse));

        ResponseEntity<WarehouseResponse> response = warehouseController.update(1L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Chennai, Tamil Nadu - Updated", response.getBody().getLocation());
        assertEquals(6000, response.getBody().getCapacity());
        verify(warehouseService, times(1)).update(eq(1L), any(UpdateWarehouseRequest.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenWarehouseDoesNotExist() {
        when(warehouseService.update(eq(999L), any(UpdateWarehouseRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<WarehouseResponse> response = warehouseController.update(999L, updateRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void update_ShouldPassCorrectIdAndRequestToService() {
        when(warehouseService.update(1L, updateRequest)).thenReturn(ResponseEntity.ok(warehouseResponse));

        warehouseController.update(1L, updateRequest);

        verify(warehouseService).update(1L, updateRequest);
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenWarehouseDeletedSuccessfully() {
        when(warehouseService.delete(1L)).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Void> response = warehouseController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(warehouseService, times(1)).delete(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenWarehouseDoesNotExist() {
        when(warehouseService.delete(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Void> response = warehouseController.delete(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void delete_ShouldNotCallAnyOtherServiceMethod_WhenDeleting() {
        when(warehouseService.delete(1L)).thenReturn(ResponseEntity.noContent().build());

        warehouseController.delete(1L);

        verify(warehouseService).delete(1L);
        verify(warehouseService, never()).getAll();
        verify(warehouseService, never()).getById(anyLong());
    }
}