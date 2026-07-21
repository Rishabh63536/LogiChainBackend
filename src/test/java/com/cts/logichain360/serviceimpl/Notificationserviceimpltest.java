package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.dto.response.NotificationResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.NotificationType;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.repository.NotificationRepository;
import com.cts.logichain360.repository.UserRepository;
import com.cts.logichain360.repository.WarehouseManagerRepository;
import com.cts.logichain360.service.impl.NotificationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepo;
    @Mock private WarehouseManagerRepository wmRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks private NotificationServiceImpl notificationService;

    private User mockUser;
    private Notification mockNotification;
    private WarehouseManager mockManager;
    private Warehouse mockWarehouse;
    private Product mockProduct;
    private ProductWarehouse mockPW;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(5L).name("Mohan Kumar").phone("9876543210").build();

        mockWarehouse = Warehouse.builder().id(100L).warehouseCode("WH-CHN-01").build();

        mockManager = WarehouseManager.builder().id(1L).user(mockUser)
                .employeeCode("EMP001").assignedWarehouse(mockWarehouse).build();

        User vendorUser = User.builder().id(30L).name("Sony").phone("9999999999").build();
        Vendor vendor = Vendor.builder().id(30L).user(vendorUser).companyName("Sony India").build();

        mockProduct = Product.builder().productId(5L)
                .productName("Sony WH-1000XM5").productPrice(29990.0).vendor(vendor).build();

        mockPW = ProductWarehouse.builder().id(10L)
                .product(mockProduct).warehouse(mockWarehouse)
                .stock(90).maxStock(500).rolPercent(40.0).build();

        mockNotification = Notification.builder().id(1L)
                .recipient(mockUser).type(NotificationType.ROL_BREACH)
                .message("Product below ROL.")
                .createdAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .read(false).relatedEntityId(10L).relatedEntityType("ProductWarehouse").build();
    }

    // ─── notifyRolBreach ──────────────────────────────────────────────────────

    @Test
    void notifyRolBreach_ShouldSaveNotification_WhenManagerAssigned() {
        when(wmRepo.findByAssignedWarehouse_Id(100L)).thenReturn(Optional.of(mockManager));
        when(notificationRepo.save(any(Notification.class))).thenReturn(mockNotification);

        notificationService.notifyRolBreach(mockPW);

        verify(notificationRepo).save(argThat(n ->
                n.getType() == NotificationType.ROL_BREACH
                && !n.isRead()
                && n.getRelatedEntityId().equals(10L)
                && n.getRelatedEntityType().equals("ProductWarehouse")
                && n.getRecipient().equals(mockUser)));
    }

    @Test
    void notifyRolBreach_ShouldNotSaveNotification_WhenNoManagerAssigned() {
        when(wmRepo.findByAssignedWarehouse_Id(100L)).thenReturn(Optional.empty());

        notificationService.notifyRolBreach(mockPW);

        verify(notificationRepo, never()).save(any());
    }

    @Test
    void notifyRolBreach_ShouldIncludeProductAndWarehouseInMessage() {
        when(wmRepo.findByAssignedWarehouse_Id(100L)).thenReturn(Optional.of(mockManager));
        when(notificationRepo.save(any(Notification.class))).thenReturn(mockNotification);

        notificationService.notifyRolBreach(mockPW);

        verify(notificationRepo).save(argThat(n ->
                n.getMessage().contains("Sony WH-1000XM5")
                && n.getMessage().contains("WH-CHN-01")));
    }

    // ─── getNotificationsForUser ──────────────────────────────────────────────

    @Test
    void getNotificationsForUser_ShouldReturnAll_WhenUserExists() {
        Notification n2 = Notification.builder().id(2L).recipient(mockUser)
                .message("Another").read(true).createdAt(LocalDateTime.now()).build();

        when(userRepo.existsById(5L)).thenReturn(true);
        when(notificationRepo.findAllByRecipient_IdOrderByCreatedAtDesc(5L))
                .thenReturn(Arrays.asList(mockNotification, n2));

        ResponseEntity<List<NotificationResponse>> response =
                notificationService.getNotificationsForUser(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(NotificationType.ROL_BREACH, response.getBody().get(0).getType());
        assertFalse(response.getBody().get(0).isRead());
    }

    @Test
    void getNotificationsForUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getNotificationsForUser(999L));
    }

    @Test
    void getNotificationsForUser_ShouldReturnEmpty_WhenNoNotifications() {
        when(userRepo.existsById(5L)).thenReturn(true);
        when(notificationRepo.findAllByRecipient_IdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of());

        assertTrue(notificationService.getNotificationsForUser(5L).getBody().isEmpty());
    }

    // ─── getUnreadNotificationsForUser ────────────────────────────────────────

    @Test
    void getUnreadNotificationsForUser_ShouldReturnOnlyUnread_WhenUserExists() {
        when(userRepo.existsById(5L)).thenReturn(true);
        when(notificationRepo.findAllByRecipient_IdAndReadFalseOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(mockNotification));

        ResponseEntity<List<NotificationResponse>> response =
                notificationService.getUnreadNotificationsForUser(5L);

        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).isRead());
        assertEquals(5L, response.getBody().get(0).getRecipientUserId());
    }

    @Test
    void getUnreadNotificationsForUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepo.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getUnreadNotificationsForUser(999L));
    }

    @Test
    void getUnreadNotificationsForUser_ShouldReturnEmpty_WhenAllRead() {
        when(userRepo.existsById(5L)).thenReturn(true);
        when(notificationRepo.findAllByRecipient_IdAndReadFalseOrderByCreatedAtDesc(5L))
                .thenReturn(List.of());

        assertTrue(notificationService.getUnreadNotificationsForUser(5L).getBody().isEmpty());
    }

    // ─── markAsRead ───────────────────────────────────────────────────────────

    @Test
    void markAsRead_ShouldSetReadTrueAndReturnResponse() {
        when(notificationRepo.findById(1L)).thenReturn(Optional.of(mockNotification));
        when(notificationRepo.save(any())).thenReturn(mockNotification);

        ResponseEntity<NotificationResponse> response = notificationService.markAsRead(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(mockNotification.isRead());
        assertEquals(1L, response.getBody().getId());
        assertEquals(5L, response.getBody().getRecipientUserId());
        assertEquals("ProductWarehouse", response.getBody().getRelatedEntityType());
        verify(notificationRepo).save(mockNotification);
    }

    @Test
    void markAsRead_ShouldThrowException_WhenNotificationNotFound() {
        when(notificationRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAsRead(999L));
        verify(notificationRepo, never()).save(any());
    }
}