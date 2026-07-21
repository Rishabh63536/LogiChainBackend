package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.request.UpdateCustomerRequest;
import com.cts.logichain360.dto.response.CustomerResponse;
import com.cts.logichain360.entity.Customer;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.repository.CustomerRepository;
import com.cts.logichain360.service.impl.CustomerServiceImpl;

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
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepo;
    @InjectMocks private CustomerServiceImpl customerService;

    private User mockUser;
    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(10L).name("Alice Smith")
                .phone("9876543210").role(UserRole.CUSTOMER).status(UserStatus.ACTIVE).build();

        mockCustomer = Customer.builder().id(1L).user(mockUser)
                .companyName("Alice Corp").gstNumber("22AAAAA0000A1Z5")
                .email("alice@example.com").shippingAddress("123 Main St")
                .billingAddress("123 Main St").creditLimit(50000.0).paymentTerms("Net 30").build();
    }

    @Test
    void getCustomerById_ShouldReturnCustomer_WhenExists() {
        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));

        ResponseEntity<CustomerResponse> response = customerService.getCustomerById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Alice Smith", response.getBody().getUserName());
        assertEquals("9876543210", response.getBody().getUserPhone());
        assertEquals("Alice Corp", response.getBody().getCompanyName());
        assertEquals("alice@example.com", response.getBody().getEmail());
        assertEquals(50000.0, response.getBody().getCreditLimit());
    }

    @Test
    void getCustomerById_ShouldReturnNotFound_WhenNotExists() {
        when(customerRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, customerService.getCustomerById(999L).getStatusCode());
    }

    @Test
    void getCustomerByUserId_ShouldReturnCustomer_WhenUserExists() {
        when(customerRepo.findByUser_Id(10L)).thenReturn(Optional.of(mockCustomer));

        ResponseEntity<CustomerResponse> response = customerService.getCustomerByUserId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getUserId());
    }

    @Test
    void getCustomerByUserId_ShouldReturnNotFound_WhenNotExists() {
        when(customerRepo.findByUser_Id(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, customerService.getCustomerByUserId(999L).getStatusCode());
    }

    @Test
    void getAllCustomers_ShouldReturnAll() {
        Customer c2 = Customer.builder().id(2L).user(mockUser).companyName("Bob Ltd").build();
        when(customerRepo.findAll()).thenReturn(Arrays.asList(mockCustomer, c2));

        ResponseEntity<List<CustomerResponse>> response = customerService.getAllCustomers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getAllCustomers_ShouldReturnEmptyList_WhenNone() {
        when(customerRepo.findAll()).thenReturn(List.of());

        assertTrue(customerService.getAllCustomers().getBody().isEmpty());
    }

    @Test
    void updateCustomer_ShouldUpdateAllProvidedFields() {
        UpdateCustomerRequest req = UpdateCustomerRequest.builder()
                .companyName("Alice Updated").gstNumber("NEW_GST")
                .email("new@email.com").shippingAddress("New Addr")
                .billingAddress("New Billing").creditLimit(75000.0).paymentTerms("Net 60").build();

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(customerRepo.save(any())).thenReturn(mockCustomer);

        ResponseEntity<CustomerResponse> response = customerService.updateCustomer(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alice Updated", mockCustomer.getCompanyName());
        assertEquals("NEW_GST", mockCustomer.getGstNumber());
        assertEquals("new@email.com", mockCustomer.getEmail());
        assertEquals("New Addr", mockCustomer.getShippingAddress());
        assertEquals("New Billing", mockCustomer.getBillingAddress());
        assertEquals(75000.0, mockCustomer.getCreditLimit());
        assertEquals("Net 60", mockCustomer.getPaymentTerms());
        verify(customerRepo).save(mockCustomer);
    }

    @Test
    void updateCustomer_ShouldNotOverwriteNullFields() {
        UpdateCustomerRequest req = UpdateCustomerRequest.builder().build(); // all null

        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(customerRepo.save(any())).thenReturn(mockCustomer);

        customerService.updateCustomer(1L, req);

        assertEquals("Alice Corp", mockCustomer.getCompanyName());
        assertEquals("alice@example.com", mockCustomer.getEmail());
    }

    @Test
    void updateCustomer_ShouldReturnNotFound_WhenCustomerDoesNotExist() {
        when(customerRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                customerService.updateCustomer(999L, UpdateCustomerRequest.builder().build())
                        .getStatusCode());
        verify(customerRepo, never()).save(any());
    }

    @Test
    void deleteCustomer_ShouldReturnNoContent_WhenExists() {
        when(customerRepo.findById(1L)).thenReturn(Optional.of(mockCustomer));

        ResponseEntity<Void> response = customerService.deleteCustomer(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerRepo).delete(mockCustomer);
    }

    @Test
    void deleteCustomer_ShouldReturnNotFound_WhenNotExists() {
        when(customerRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, customerService.deleteCustomer(999L).getStatusCode());
        verify(customerRepo, never()).delete(any());
    }
}