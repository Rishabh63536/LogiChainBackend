package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.request.UpdateVendorRequest;
import com.cts.logichain360.dto.response.VendorResponse;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.entity.Vendor;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.repository.VendorRepository;
import com.cts.logichain360.service.impl.VendorServiceImpl;

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
class VendorServiceImplTest {

    @Mock private VendorRepository vendorRepo;
    @InjectMocks private VendorServiceImpl vendorService;

    private User mockUser;
    private Vendor mockVendor;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(30L).name("Priya Vendor")
                .phone("9876543210").role(UserRole.VENDOR).status(UserStatus.ACTIVE).build();

        mockVendor = Vendor.builder().id(1L).user(mockUser)
                .companyName("Priya Supplies Ltd").gstNumber("22BBBBB0000B1Z6")
                .email("priya@supplies.com").businessAddress("456 Industrial Area")
                .contactPerson("Priya Sharma").paymentTerms("Net 60").build();
    }

    @Test
    void getVendorById_ShouldReturnVendor_WhenExists() {
        when(vendorRepo.findById(1L)).thenReturn(Optional.of(mockVendor));

        ResponseEntity<VendorResponse> response = vendorService.getVendorById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals(30L, response.getBody().getUserId());
        assertEquals("Priya Vendor", response.getBody().getUserName());
        assertEquals("9876543210", response.getBody().getUserPhone());
        assertEquals("Priya Supplies Ltd", response.getBody().getCompanyName());
        assertEquals("priya@supplies.com", response.getBody().getEmail());
    }

    @Test
    void getVendorById_ShouldReturnNotFound_WhenNotExists() {
        when(vendorRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, vendorService.getVendorById(999L).getStatusCode());
    }

    @Test
    void getVendorByUserId_ShouldReturnVendor_WhenExists() {
        when(vendorRepo.findByUser_Id(30L)).thenReturn(Optional.of(mockVendor));

        ResponseEntity<VendorResponse> response = vendorService.getVendorByUserId(30L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(30L, response.getBody().getUserId());
    }

    @Test
    void getVendorByUserId_ShouldReturnNotFound_WhenNotExists() {
        when(vendorRepo.findByUser_Id(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, vendorService.getVendorByUserId(999L).getStatusCode());
    }

    @Test
    void getAllVendors_ShouldReturnAll() {
        Vendor v2 = Vendor.builder().id(2L).user(mockUser).companyName("Kiran Traders").build();
        when(vendorRepo.findAll()).thenReturn(Arrays.asList(mockVendor, v2));

        assertEquals(2, vendorService.getAllVendors().getBody().size());
    }

    @Test
    void getAllVendors_ShouldReturnEmptyList_WhenNone() {
        when(vendorRepo.findAll()).thenReturn(List.of());

        assertTrue(vendorService.getAllVendors().getBody().isEmpty());
    }

    @Test
    void updateVendor_ShouldUpdateAllProvidedFields() {
        UpdateVendorRequest req = UpdateVendorRequest.builder()
                .companyName("Updated Corp").gstNumber("NEW_GST")
                .email("new@vendor.com").businessAddress("New Address")
                .contactPerson("New Person").paymentTerms("Net 30").build();

        when(vendorRepo.findById(1L)).thenReturn(Optional.of(mockVendor));
        when(vendorRepo.save(any())).thenReturn(mockVendor);

        ResponseEntity<VendorResponse> response = vendorService.updateVendor(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Corp", mockVendor.getCompanyName());
        assertEquals("NEW_GST", mockVendor.getGstNumber());
        assertEquals("new@vendor.com", mockVendor.getEmail());
        assertEquals("New Address", mockVendor.getBusinessAddress());
        assertEquals("New Person", mockVendor.getContactPerson());
        assertEquals("Net 30", mockVendor.getPaymentTerms());
        verify(vendorRepo).save(mockVendor);
    }

    @Test
    void updateVendor_ShouldNotOverwriteNullFields() {
        UpdateVendorRequest req = UpdateVendorRequest.builder().build();

        when(vendorRepo.findById(1L)).thenReturn(Optional.of(mockVendor));
        when(vendorRepo.save(any())).thenReturn(mockVendor);

        vendorService.updateVendor(1L, req);

        assertEquals("Priya Supplies Ltd", mockVendor.getCompanyName());
        assertEquals("priya@supplies.com", mockVendor.getEmail());
    }

    @Test
    void updateVendor_ShouldReturnNotFound_WhenNotExists() {
        when(vendorRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                vendorService.updateVendor(999L, UpdateVendorRequest.builder().build()).getStatusCode());
        verify(vendorRepo, never()).save(any());
    }

    @Test
    void deleteVendor_ShouldReturnNoContent_WhenExists() {
        when(vendorRepo.findById(1L)).thenReturn(Optional.of(mockVendor));

        assertEquals(HttpStatus.NO_CONTENT, vendorService.deleteVendor(1L).getStatusCode());
        verify(vendorRepo).delete(mockVendor);
    }

    @Test
    void deleteVendor_ShouldReturnNotFound_WhenNotExists() {
        when(vendorRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, vendorService.deleteVendor(999L).getStatusCode());
        verify(vendorRepo, never()).delete(any());
    }
}
