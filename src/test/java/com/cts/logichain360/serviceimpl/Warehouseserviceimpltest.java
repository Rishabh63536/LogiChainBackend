package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.request.CreateWarehouseRequest;
import com.cts.logichain360.dto.request.UpdateWarehouseRequest;
import com.cts.logichain360.dto.response.WarehouseResponse;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.entity.Warehouse;
import com.cts.logichain360.entity.WarehouseManager;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.repository.WarehouseManagerRepository;
import com.cts.logichain360.repository.WarehouseRepository;
import com.cts.logichain360.service.impl.WarehouseServiceImpl;

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
class WarehouseServiceImplTest {

    @Mock private WarehouseRepository warehouseRepo;
    @Mock private WarehouseManagerRepository wmRepo;
    @InjectMocks private WarehouseServiceImpl warehouseService;

    private Warehouse mockWarehouse;
    private User mockUser;
    private WarehouseManager mockManager;

    @BeforeEach
    void setUp() {
        mockWarehouse = Warehouse.builder().id(1L)
                .warehouseCode("WH-CHN-01").location("Chennai").capacity(5000).build();

        mockUser = User.builder().id(40L).name("Mohan Kumar").phone("9876543210").build();

        mockManager = WarehouseManager.builder().id(1L).user(mockUser)
                .assignedWarehouse(mockWarehouse).build();
    }

    @Test
    void createWarehouse_ShouldReturnCreated_WhenCodeIsUnique() {
        CreateWarehouseRequest req = CreateWarehouseRequest.builder()
                .warehouseCode("WH-CHN-01").location("Chennai").capacity(5000).build();

        when(warehouseRepo.existsByWarehouseCode("WH-CHN-01")).thenReturn(false);
        when(warehouseRepo.save(any())).thenReturn(mockWarehouse);
        when(wmRepo.findAll()).thenReturn(List.of());

        ResponseEntity<WarehouseResponse> response = warehouseService.createWarehouse(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("WH-CHN-01", response.getBody().getWarehouseCode());
        assertEquals("Chennai", response.getBody().getLocation());
        assertEquals(5000, response.getBody().getCapacity());
    }

    @Test
    void createWarehouse_ShouldThrowException_WhenCodeAlreadyExists() {
        CreateWarehouseRequest req = CreateWarehouseRequest.builder()
                .warehouseCode("WH-CHN-01").build();
        when(warehouseRepo.existsByWarehouseCode("WH-CHN-01")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> warehouseService.createWarehouse(req));
        verify(warehouseRepo, never()).save(any());
    }

    @Test
    void getById_ShouldReturnWarehouseWithManagerDetails_WhenManagerAssigned() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(mockWarehouse));
        when(wmRepo.findAll()).thenReturn(List.of(mockManager));

        ResponseEntity<WarehouseResponse> response = warehouseService.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals(1L, response.getBody().getManagerId());
        assertEquals("Mohan Kumar", response.getBody().getManagerName());
        assertEquals("EMP001", response.getBody().getManagerEmployeeCode());
    }

    @Test
    void getById_ShouldReturnNullManagerFields_WhenNoManagerAssigned() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(mockWarehouse));
        when(wmRepo.findAll()).thenReturn(List.of());

        WarehouseResponse response = warehouseService.getById(1L).getBody();

        assertNull(response.getManagerId());
        assertNull(response.getManagerName());
        assertNull(response.getManagerEmployeeCode());
    }

    @Test
    void getById_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(warehouseRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warehouseService.getById(999L));
    }

    @Test
    void getAll_ShouldReturnAllWarehouses() {
        Warehouse w2 = Warehouse.builder().id(2L).warehouseCode("WH-BLR-01")
                .location("Bangalore").capacity(3000).build();

        when(warehouseRepo.findAll()).thenReturn(Arrays.asList(mockWarehouse, w2));
        when(wmRepo.findAll()).thenReturn(List.of());

        ResponseEntity<List<WarehouseResponse>> response = warehouseService.getAll();

        assertEquals(2, response.getBody().size());
    }

    @Test
    void update_ShouldUpdateFields_WhenWarehouseExists() {
        UpdateWarehouseRequest req = UpdateWarehouseRequest.builder()
                .location("Chennai Updated").capacity(6000).build();

        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(mockWarehouse));
        when(warehouseRepo.save(any())).thenReturn(mockWarehouse);
        when(wmRepo.findAll()).thenReturn(List.of());

        ResponseEntity<WarehouseResponse> response = warehouseService.update(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Chennai Updated", mockWarehouse.getLocation());
        assertEquals(6000, mockWarehouse.getCapacity());
    }

    @Test
    void update_ShouldThrowException_WhenNewCodeAlreadyTaken() {
        UpdateWarehouseRequest req = UpdateWarehouseRequest.builder()
                .warehouseCode("WH-BLR-01").build();

        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(mockWarehouse));
        when(warehouseRepo.existsByWarehouseCode("WH-BLR-01")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> warehouseService.update(1L, req));
    }

    @Test
    void update_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(warehouseRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.update(999L, UpdateWarehouseRequest.builder().build()));
    }

    @Test
    void delete_ShouldReturnNoContent_WhenExists() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(mockWarehouse));
        when(wmRepo.findAll()).thenReturn(List.of());

        assertEquals(HttpStatus.NO_CONTENT, warehouseService.delete(1L).getStatusCode());
        verify(warehouseRepo).delete(mockWarehouse);
    }

    @Test
    void delete_ShouldUnlinkManagerBeforeDeleting() {
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(mockWarehouse));
        when(wmRepo.findAll()).thenReturn(List.of(mockManager));

        warehouseService.delete(1L);

        assertNull(mockManager.getAssignedWarehouse());
        verify(wmRepo).save(mockManager);
        verify(warehouseRepo).delete(mockWarehouse);
    }

    @Test
    void delete_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(warehouseRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warehouseService.delete(999L));
        verify(warehouseRepo, never()).delete(any());
    }
}