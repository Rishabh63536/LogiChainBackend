package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.request.UpdateWarehouseManagerRequest;
import com.cts.logichain360.dto.request.WarehouseAssignmentRequest;
import com.cts.logichain360.dto.response.WarehouseManagerResponse;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.entity.Warehouse;
import com.cts.logichain360.entity.WarehouseManager;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.repository.WarehouseManagerRepository;
import com.cts.logichain360.repository.WarehouseRepository;
import com.cts.logichain360.service.impl.WarehouseManagerServiceImpl;

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
class WarehouseManagerServiceImplTest {

    @Mock private WarehouseManagerRepository wmRepo;
    @Mock private WarehouseRepository warehouseRepo;
    @InjectMocks private WarehouseManagerServiceImpl wmService;

    private User mockUser;
    private Warehouse mockWarehouse;
    private WarehouseManager mockManager;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(40L).name("Mohan Kumar")
                .phone("9876543210").role(UserRole.WAREHOUSE_MANAGER)
                .status(UserStatus.ACTIVE).build();

        mockWarehouse = Warehouse.builder().id(100L)
                .warehouseCode("WH-CHN-01").location("Chennai").capacity(5000).build();

        mockManager = WarehouseManager.builder().id(1L).user(mockUser)
                .employeeCode("EMP001").designation("Senior Manager").build();
    }

    @Test
    void getById_ShouldReturnManager_WhenExists() {
        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));

        ResponseEntity<WarehouseManagerResponse> response = wmService.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals(40L, response.getBody().getUserId());
        assertEquals("Mohan Kumar", response.getBody().getUserName());
        assertEquals("EMP001", response.getBody().getEmployeeCode());
        assertEquals(100L, response.getBody().getAssignedWarehouseId());
        assertEquals("WH-CHN-01", response.getBody().getAssignedWarehouseCode());
        assertEquals("Chennai", response.getBody().getAssignedWarehouseLocation());
    }

    @Test
    void getById_ShouldReturnNullWarehouseFields_WhenNoWarehouseAssigned() {
        mockManager.setAssignedWarehouse(null);
        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));

        WarehouseManagerResponse response = wmService.getById(1L).getBody();

        assertNull(response.getAssignedWarehouseId());
        assertNull(response.getAssignedWarehouseCode());
        assertNull(response.getAssignedWarehouseLocation());
    }

    @Test
    void getById_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(wmRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wmService.getById(999L));
    }

    @Test
    void getByUserId_ShouldReturnManager_WhenExists() {
        when(wmRepo.findByUser_Id(40L)).thenReturn(Optional.of(mockManager));

        ResponseEntity<WarehouseManagerResponse> response = wmService.getByUserId(40L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(40L, response.getBody().getUserId());
    }

    @Test
    void getByUserId_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(wmRepo.findByUser_Id(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wmService.getByUserId(999L));
    }

    @Test
    void getAll_ShouldReturnAllManagers() {
        WarehouseManager m2 = WarehouseManager.builder().id(2L).user(mockUser)
                .employeeCode("EMP002").build();
        when(wmRepo.findAll()).thenReturn(Arrays.asList(mockManager, m2));

        ResponseEntity<List<WarehouseManagerResponse>> response = wmService.getAll();

        assertEquals(2, response.getBody().size());
    }

    @Test
    void update_ShouldUpdateAllProvidedFields() {
        UpdateWarehouseManagerRequest req = UpdateWarehouseManagerRequest.builder()
                .employeeCode("EMP999").designation("Lead Manager").build();

        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));
        when(wmRepo.save(any())).thenReturn(mockManager);

        ResponseEntity<WarehouseManagerResponse> response = wmService.update(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EMP999", mockManager.getEmployeeCode());
        assertEquals("Lead Manager", mockManager.getDesignation());
    }

    @Test
    void update_ShouldNotOverwriteNullFields() {
        UpdateWarehouseManagerRequest req = UpdateWarehouseManagerRequest.builder().build();

        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));
        when(wmRepo.save(any())).thenReturn(mockManager);

        wmService.update(1L, req);

        assertEquals("EMP001", mockManager.getEmployeeCode());
        assertEquals("Senior Manager", mockManager.getDesignation());
    }

    @Test
    void update_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(wmRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> wmService.update(999L, UpdateWarehouseManagerRequest.builder().build()));
    }

    @Test
    void assignWarehouse_ShouldAssignWarehouse_WhenAvailable() {
        mockManager.setAssignedWarehouse(null);
        WarehouseAssignmentRequest req = WarehouseAssignmentRequest.builder().warehouseId(100L).build();

        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));
        when(warehouseRepo.findById(100L)).thenReturn(Optional.of(mockWarehouse));
        when(wmRepo.findAll()).thenReturn(List.of(mockManager));
        when(wmRepo.save(any())).thenReturn(mockManager);

        ResponseEntity<WarehouseManagerResponse> response = wmService.assignWarehouse(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockWarehouse, mockManager.getAssignedWarehouse());
    }

    @Test
    void assignWarehouse_ShouldUnassign_WhenWarehouseIdIsNull() {
        WarehouseAssignmentRequest req = WarehouseAssignmentRequest.builder().warehouseId(null).build();

        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));
        when(wmRepo.save(any())).thenReturn(mockManager);

        wmService.assignWarehouse(1L, req);

        assertNull(mockManager.getAssignedWarehouse());
        verify(wmRepo).save(mockManager);
    }

    @Test
    void assignWarehouse_ShouldThrowException_WhenWarehouseAlreadyTaken() {
        WarehouseManager anotherManager = WarehouseManager.builder()
                .id(99L).user(mockUser).assignedWarehouse(mockWarehouse).build();
        WarehouseAssignmentRequest req = WarehouseAssignmentRequest.builder().warehouseId(100L).build();

        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));
        when(warehouseRepo.findById(100L)).thenReturn(Optional.of(mockWarehouse));
        when(wmRepo.findAll()).thenReturn(Arrays.asList(mockManager, anotherManager));

        assertThrows(UserAlreadyExistsException.class, () -> wmService.assignWarehouse(1L, req));
    }

    @Test
    void assignWarehouse_ShouldThrowException_WhenWarehouseNotFound() {
        WarehouseAssignmentRequest req = WarehouseAssignmentRequest.builder().warehouseId(999L).build();

        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));
        when(warehouseRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wmService.assignWarehouse(1L, req));
    }

    @Test
    void delete_ShouldReturnNoContent_WhenExists() {
        when(wmRepo.findById(1L)).thenReturn(Optional.of(mockManager));

        assertEquals(HttpStatus.NO_CONTENT, wmService.delete(1L).getStatusCode());
        verify(wmRepo).delete(mockManager);
    }

    @Test
    void delete_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(wmRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wmService.delete(999L));
        verify(wmRepo, never()).delete(any());
    }
}
