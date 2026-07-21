package com.cts.logichain360;

import com.cts.logichain360.controller.NotificationController;
import com.cts.logichain360.dto.response.NotificationResponse;
import com.cts.logichain360.service.NotificationService;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponse unreadNotification;
    private NotificationResponse readNotification;

    @BeforeEach
    void setUp() {
        unreadNotification = NotificationResponse.builder()
                .id(1L).recipientUserId(5L)
                .message("Product 'Sony WH-1000XM5' at WH-CHN-01 is below ROL (26%).")
                .createdAt(LocalDateTime.of(2026, 6, 1, 10, 32))
                .read(false).relatedEntityId(10L).relatedEntityType("ProductWarehouse")
                .build();

        readNotification = NotificationResponse.builder()
                .id(2L).recipientUserId(5L)
                .message("Product 'LG Monitor' at WH-CHN-01 is below ROL (18%).")
                .createdAt(LocalDateTime.of(2026, 6, 2, 9, 0))
                .read(true).relatedEntityId(11L).relatedEntityType("ProductWarehouse")
                .build();
    }

    // ─── getForUser ───────────────────────────────────────────────────────────

    @Test
    void getForUser_ShouldReturnAllNotifications_WhenUserHasNotifications() {
        when(notificationService.getNotificationsForUser(5L))
                .thenReturn(ResponseEntity.ok(Arrays.asList(unreadNotification, readNotification)));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getForUser(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().anyMatch(n -> !n.isRead()));
        assertTrue(response.getBody().stream().anyMatch(NotificationResponse::isRead));
        verify(notificationService, times(1)).getNotificationsForUser(5L);
    }

    @Test
    void getForUser_ShouldReturnEmptyList_WhenUserHasNoNotifications() {
        when(notificationService.getNotificationsForUser(99L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getForUser(99L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getForUser_ShouldCallServiceWithCorrectUserId() {
        when(notificationService.getNotificationsForUser(5L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        notificationController.getForUser(5L);

        verify(notificationService).getNotificationsForUser(5L);
        verifyNoMoreInteractions(notificationService);
    }

    // ─── getUnread ────────────────────────────────────────────────────────────

    @Test
    void getUnread_ShouldReturnOnlyUnreadNotifications() {
        when(notificationService.getUnreadNotificationsForUser(5L))
                .thenReturn(ResponseEntity.ok(Collections.singletonList(unreadNotification)));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getUnread(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).isRead());
        verify(notificationService, times(1)).getUnreadNotificationsForUser(5L);
    }

    @Test
    void getUnread_ShouldReturnEmptyList_WhenAllNotificationsAreRead() {
        when(notificationService.getUnreadNotificationsForUser(5L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getUnread(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getUnread_ShouldNotCallGetAllNotifications() {
        when(notificationService.getUnreadNotificationsForUser(5L))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        notificationController.getUnread(5L);

        verify(notificationService).getUnreadNotificationsForUser(5L);
        verify(notificationService, never()).getNotificationsForUser(anyLong());
    }

    // ─── markAsRead ───────────────────────────────────────────────────────────

    @Test
    void markAsRead_ShouldReturnNotificationWithReadTrue() {
        NotificationResponse markedRead = NotificationResponse.builder()
                .id(1L).recipientUserId(5L).read(true)
                .message("Product 'Sony WH-1000XM5' at WH-CHN-01 is below ROL (26%).").build();

        when(notificationService.markAsRead(1L)).thenReturn(ResponseEntity.ok(markedRead));

        ResponseEntity<NotificationResponse> response = notificationController.markAsRead(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isRead());
        assertEquals(1L, response.getBody().getId());
        verify(notificationService, times(1)).markAsRead(1L);
    }

    @Test
    void markAsRead_ShouldReturnNotFound_WhenNotificationDoesNotExist() {
        when(notificationService.markAsRead(999L)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<NotificationResponse> response = notificationController.markAsRead(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(notificationService).markAsRead(999L);
    }

    @Test
    void markAsRead_ShouldOnlyCallMarkAsRead_NotGetMethods() {
        when(notificationService.markAsRead(1L)).thenReturn(ResponseEntity.ok(readNotification));

        notificationController.markAsRead(1L);

        verify(notificationService).markAsRead(1L);
        verify(notificationService, never()).getNotificationsForUser(anyLong());
        verify(notificationService, never()).getUnreadNotificationsForUser(anyLong());
    }
}