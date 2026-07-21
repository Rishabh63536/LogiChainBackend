package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.request.DriverAvailabilityRequest;
import com.cts.logichain360.dto.request.UpdateDriverRequest;
import com.cts.logichain360.dto.response.DriverResponse;
import com.cts.logichain360.entity.Driver;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.repository.DriverRepository;
import com.cts.logichain360.service.impl.DriverServiceImpl;

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock private DriverRepository driverRepo;
    @InjectMocks private DriverServiceImpl driverService;

    private User mockUser;
    private Driver mockDriver;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(20L).name("Ravi Kumar")
                .phone("9876543210").role(UserRole.DRIVER).status(UserStatus.ACTIVE).build();

        mockDriver = Driver.builder().id(1L).user(mockUser)
                .licenseNumber("TN1234567890")
                .licenseExpiry(LocalDate.of(2027, 12, 31))
                .available(true).build();
    }

    @Test
    void getDriverById_ShouldReturnDriver_WhenExists() {
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));

        ResponseEntity<DriverResponse> response = driverService.getDriverById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals(20L, response.getBody().getUserId());
        assertEquals("Ravi Kumar", response.getBody().getUserName());
        assertEquals("9876543210", response.getBody().getUserPhone());
        assertEquals("TN1234567890", response.getBody().getLicenseNumber());
        assertTrue(response.getBody().getAvailable());
    }

    @Test
    void getDriverById_ShouldReturnNotFound_WhenNotExists() {
        when(driverRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, driverService.getDriverById(999L).getStatusCode());
    }

    @Test
    void getDriverByUserId_ShouldReturnDriver_WhenExists() {
        when(driverRepo.findByUser_Id(20L)).thenReturn(Optional.of(mockDriver));

        ResponseEntity<DriverResponse> response = driverService.getDriverByUserId(20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(20L, response.getBody().getUserId());
    }

    @Test
    void getDriverByUserId_ShouldReturnNotFound_WhenNotExists() {
        when(driverRepo.findByUser_Id(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, driverService.getDriverByUserId(999L).getStatusCode());
    }

    @Test
    void getAllDrivers_ShouldReturnAll() {
        Driver d2 = Driver.builder().id(2L).user(mockUser).available(false).build();
        when(driverRepo.findAll()).thenReturn(Arrays.asList(mockDriver, d2));

        ResponseEntity<List<DriverResponse>> response = driverService.getAllDrivers();

        assertEquals(2, response.getBody().size());
    }

    @Test
    void getAvailableDrivers_ShouldReturnOnlyAvailable() {
        when(driverRepo.findAllByAvailableTrue()).thenReturn(List.of(mockDriver));

        ResponseEntity<List<DriverResponse>> response = driverService.getAvailableDrivers();

        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getAvailable());
    }

    @Test
    void getAvailableDrivers_ShouldReturnEmpty_WhenNoneAvailable() {
        when(driverRepo.findAllByAvailableTrue()).thenReturn(List.of());

        assertTrue(driverService.getAvailableDrivers().getBody().isEmpty());
    }

    @Test
    void updateDriver_ShouldUpdateAllProvidedFields() {
        UpdateDriverRequest req = UpdateDriverRequest.builder()
                .licenseNumber("TN9999999999")
                .licenseExpiry(LocalDate.of(2030, 6, 30)).build();
        
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));
        when(driverRepo.save(any())).thenReturn(mockDriver);

        ResponseEntity<DriverResponse> response = driverService.updateDriver(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TN9999999999", mockDriver.getLicenseNumber());
        assertEquals(LocalDate.of(2030, 6, 30), mockDriver.getLicenseExpiry());
    }

    @Test
    void updateDriver_ShouldNotOverwriteNullFields() {
        UpdateDriverRequest req = UpdateDriverRequest.builder().build(); // all null

        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));
        when(driverRepo.save(any())).thenReturn(mockDriver);

        driverService.updateDriver(1L, req);

        assertEquals("TN1234567890", mockDriver.getLicenseNumber());
    }

    @Test
    void updateDriver_ShouldReturnNotFound_WhenNotExists() {
        when(driverRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                driverService.updateDriver(999L, UpdateDriverRequest.builder().build()).getStatusCode());
    }

    @Test
    void updateAvailability_ShouldSetDriverUnavailable() {
        DriverAvailabilityRequest req = DriverAvailabilityRequest.builder().available(false).build();
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));
        when(driverRepo.save(any())).thenReturn(mockDriver);

        driverService.updateAvailability(1L, req);

        assertFalse(mockDriver.getAvailable());
        verify(driverRepo).save(mockDriver);
    }

    @Test
    void updateAvailability_ShouldSetDriverAvailable() {
        mockDriver.setAvailable(false);
        DriverAvailabilityRequest req = DriverAvailabilityRequest.builder().available(true).build();
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));
        when(driverRepo.save(any())).thenReturn(mockDriver);

        driverService.updateAvailability(1L, req);

        assertTrue(mockDriver.getAvailable());
    }

    @Test
    void updateAvailability_ShouldReturnNotFound_WhenNotExists() {
        when(driverRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                driverService.updateAvailability(999L,
                        DriverAvailabilityRequest.builder().available(true).build()).getStatusCode());
    }

    @Test
    void deleteDriver_ShouldReturnNoContent_WhenExists() {
        when(driverRepo.findById(1L)).thenReturn(Optional.of(mockDriver));

        assertEquals(HttpStatus.NO_CONTENT, driverService.deleteDriver(1L).getStatusCode());
        verify(driverRepo).delete(mockDriver);
    }

    @Test
    void deleteDriver_ShouldReturnNotFound_WhenNotExists() {
        when(driverRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, driverService.deleteDriver(999L).getStatusCode());
        verify(driverRepo, never()).delete(any());
    }
}
