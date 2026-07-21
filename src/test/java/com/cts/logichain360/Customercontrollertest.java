package com.cts.logichain360;

import com.cts.logichain360.controller.CustomerController;
import com.cts.logichain360.dto.request.UpdateCustomerRequest;
import com.cts.logichain360.dto.response.CustomerResponse;
import com.cts.logichain360.service.CustomerService;
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
 * Unit tests for CustomerController using Mockito.
 * Covers: getById, getByUserId, getAll, update, delete
 */
@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private CustomerResponse customerResponse;
    private UpdateCustomerRequest updateRequest;

    @BeforeEach
    void setUp() {
        customerResponse = CustomerResponse.builder()
                .id(1L)
                .userId(10L)
                .userName("Alice Smith")
                .userPhone("9876543210")
                .companyName("Alice Corp")
                .gstNumber("22AAAAA0000A1Z5")
                .email("alice@example.com")
                .shippingAddress("123 Main St, Chennai")
                .billingAddress("123 Main St, Chennai")
                .creditLimit(50000.0)
                .paymentTerms("Net 30")
                .build();

        updateRequest = UpdateCustomerRequest.builder()
                .companyName("Alice Corp Updated")
                .email("alice.updated@example.com")
                .creditLimit(75000.0)
                .paymentTerms("Net 45")
                .build();
    }


    @Test
    void getById_ShouldReturnCustomer_WhenCustomerExists() {
        // Arrange
        when(customerService.getCustomerById(1L)).thenReturn(ResponseEntity.ok(customerResponse));

        // Act
        ResponseEntity<CustomerResponse> response = customerController.getById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Alice Smith", response.getBody().getUserName());
        assertEquals("Alice Corp", response.getBody().getCompanyName());

        verify(customerService, times(1)).getCustomerById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenCustomerDoesNotExist() {
        // Arrange
        when(customerService.getCustomerById(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<CustomerResponse> response = customerController.getById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(customerService).getCustomerById(999L);
    }

    // ─── GetByUserId Tests ────────────────────────────────────────────────────

    @Test
    void getByUserId_ShouldReturnCustomer_WhenUserIdExists() {
        // Arrange
        when(customerService.getCustomerByUserId(10L)).thenReturn(ResponseEntity.ok(customerResponse));

        // Act
        ResponseEntity<CustomerResponse> response = customerController.getByUserId(10L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getUserId());

        verify(customerService, times(1)).getCustomerByUserId(10L);
    }

    @Test
    void getByUserId_ShouldReturnNotFound_WhenUserIdDoesNotExist() {
        // Arrange
        when(customerService.getCustomerByUserId(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<CustomerResponse> response = customerController.getByUserId(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(customerService).getCustomerByUserId(999L);
    }

    @Test
    void getByUserId_ShouldPassCorrectUserIdToService() {
        // Arrange
        Long userId = 55L;
        when(customerService.getCustomerByUserId(userId)).thenReturn(ResponseEntity.ok(customerResponse));

        // Act
        customerController.getByUserId(userId);

        // Assert
        verify(customerService).getCustomerByUserId(userId);
        verifyNoMoreInteractions(customerService);
    }

    // ─── GetAll Tests ─────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllCustomers_WhenCustomersExist() {
        // Arrange
        CustomerResponse customer2 = CustomerResponse.builder()
                .id(2L).userId(11L).userName("Bob Jones")
                .userPhone("9876543211").companyName("Bob Ltd").build();

        List<CustomerResponse> customers = Arrays.asList(customerResponse, customer2);
        when(customerService.getAllCustomers()).thenReturn(ResponseEntity.ok(customers));

        // Act
        ResponseEntity<List<CustomerResponse>> response = customerController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Alice Smith", response.getBody().get(0).getUserName());
        assertEquals("Bob Jones", response.getBody().get(1).getUserName());

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoCustomersExist() {
        // Arrange
        when(customerService.getAllCustomers()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        ResponseEntity<List<CustomerResponse>> response = customerController.getAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAll_ShouldCallServiceExactlyOnce() {
        // Arrange
        when(customerService.getAllCustomers()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        customerController.getAll();

        // Assert
        verify(customerService, times(1)).getAllCustomers();
        verifyNoMoreInteractions(customerService);
    }

    // ─── Update Tests ─────────────────────────────────────────────────────────

    @Test
    void update_ShouldReturnUpdatedCustomer_WhenValidRequest() {
        // Arrange
        CustomerResponse updatedResponse = CustomerResponse.builder()
                .id(1L).userId(10L).userName("Alice Smith")
                .companyName("Alice Corp Updated")
                .email("alice.updated@example.com")
                .creditLimit(75000.0)
                .paymentTerms("Net 45")
                .build();

        when(customerService.updateCustomer(eq(1L), any(UpdateCustomerRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedResponse));

        // Act
        ResponseEntity<CustomerResponse> response = customerController.update(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Alice Corp Updated", response.getBody().getCompanyName());
        assertEquals(75000.0, response.getBody().getCreditLimit());

        verify(customerService, times(1)).updateCustomer(eq(1L), any(UpdateCustomerRequest.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenCustomerDoesNotExist() {
        // Arrange
        when(customerService.updateCustomer(eq(999L), any(UpdateCustomerRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<CustomerResponse> response = customerController.update(999L, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void update_ShouldPassBothIdAndRequestToService() {
        // Arrange
        when(customerService.updateCustomer(1L, updateRequest))
                .thenReturn(ResponseEntity.ok(customerResponse));

        // Act
        customerController.update(1L, updateRequest);

        // Assert
        verify(customerService).updateCustomer(1L, updateRequest);
    }

    // ─── Delete Tests ─────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenCustomerDeletedSuccessfully() {
        // Arrange
        when(customerService.deleteCustomer(1L)).thenReturn(ResponseEntity.noContent().build());

        // Act
        ResponseEntity<Void> response = customerController.delete(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(customerService, times(1)).deleteCustomer(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenCustomerDoesNotExist() {
        // Arrange
        when(customerService.deleteCustomer(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<Void> response = customerController.delete(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void delete_ShouldNeverCallOtherServiceMethods_WhenDeleting() {
        // Arrange
        when(customerService.deleteCustomer(1L)).thenReturn(ResponseEntity.noContent().build());

        // Act
        customerController.delete(1L);

        // Assert
        verify(customerService).deleteCustomer(1L);
        verify(customerService, never()).getAllCustomers();
        verify(customerService, never()).getCustomerById(anyLong());
    }
}