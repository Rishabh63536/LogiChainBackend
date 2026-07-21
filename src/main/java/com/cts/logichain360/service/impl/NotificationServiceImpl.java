package com.cts.logichain360.service.impl;

import com.cts.logichain360.dto.response.NotificationResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.NotificationType;
import com.cts.logichain360.enums.OrderStatus;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.mapper.NotificationMapper;
import com.cts.logichain360.repository.NotificationRepository;
import com.cts.logichain360.repository.UserRepository;
import com.cts.logichain360.repository.WarehouseManagerRepository;
import com.cts.logichain360.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final WarehouseManagerRepository wmRepo;
    private final UserRepository userRepo;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void notifyRolBreach(ProductWarehouse pw) {
        Warehouse warehouse = pw.getWarehouse();
        WarehouseManager manager = wmRepo.findByAssignedWarehouse_Id(warehouse.getId()).orElse(null);

        if (manager == null) {
            log.warn("ROL breach on pw={} (product={}, warehouse={}) but warehouse has no assigned manager" +" so no notification created.",pw.getId(),pw.getProduct().getProductId(),warehouse.getId());
            return;
        }

        double currentPercent = (pw.getStock().doubleValue() / pw.getMaxStock().doubleValue()) * 100.0;
        String message = String.format("Product '%s' at warehouse %s is at %.1f%% of capacity (below %.1f%% reorder threshold). Please restock",pw.getProduct().getProductName(),warehouse.getWarehouseCode(),currentPercent,pw.getRolPercent());

        Notification n = Notification.builder()
                .recipient(manager.getUser())
                .type(NotificationType.ROL_BREACH)
                .message(message)
                .createdAt(LocalDateTime.now())
                .read(false)
                .relatedEntityId(pw.getId())
                .relatedEntityType("ProductWarehouse")
                .build();

        Notification saved = notificationRepo.save(n);
        log.info("ROL_BREACH notification id={} created for user={} (manager={}, pw={})",saved.getId(), manager.getUser().getId(), manager.getId(), pw.getId());
    }

    @Override
    @Transactional
    public void notifyDriverAssigned(Orders order) {
        Driver driver = order.getDriver();
        if (driver == null) {
            log.warn("notifyDriverAssigned called for order={} but no driver is set. Skipping.", order.getId());
            return;
        }

        String message = String.format("You've been assigned to deliver order #%d to %s.",order.getId(), order.getShippingAddress());

        Notification n = Notification.builder()
                .recipient(driver.getUser())
                .type(NotificationType.ORDER_ASSIGNED)
                .message(message)
                .createdAt(LocalDateTime.now())
                .read(false)
                .relatedEntityId(order.getId())
                .relatedEntityType("Order")
                .build();

        Notification saved = notificationRepo.save(n);
        log.info("ORDER_ASSIGNED notification id={} created for driver user={} (order={})",saved.getId(), driver.getUser().getId(), order.getId());
    }

    @Override
    @Transactional
    public void notifyOrderStatusChanged(Orders order) {
        Customer customer = order.getCustomer();
        if (customer == null || customer.getUser() == null) {
            log.warn("notifyOrderStatusChanged called for order={} but customer/user missing. Skipping.", order.getId());
            return;
        }

        String message = switch (order.getStatus()) {
            case ASSIGNED -> String.format("Your order #%d has been assigned to a driver and will be dispatched soon.", order.getId());
            case IN_TRANSIT-> String.format("Your order #%d is now out for delivery.", order.getId());
            case DELIVERED -> String.format("Your order #%d has been delivered successfully.", order.getId());
            case CANCELLED -> String.format("Your order #%d has been cancelled.", order.getId());
            case RETURNED-> String.format("Your return for order #%d has been processed and restocked.", order.getId());
            default -> String.format("Your order #%d status is now %s.", order.getId(), order.getStatus());
        };

        Notification n = Notification.builder()
                .recipient(customer.getUser())
                .type(NotificationType.ORDER_STATUS_CHANGED)
                .message(message)
                .createdAt(LocalDateTime.now())
                .read(false)
                .relatedEntityId(order.getId())
                .relatedEntityType("Order")
                .build();

        Notification saved = notificationRepo.save(n);
        log.info("ORDER_STATUS_CHANGED ({}) notification id={} created for customer user={} (order={})",order.getStatus(), saved.getId(), customer.getUser().getId(), order.getId());
    }

    @Override
    public ResponseEntity<List<NotificationResponse>> getNotificationsForUser(Long userId) {
        log.debug("Fetching all notifications for user={}", userId);
        if (!userRepo.existsById(userId)) {
            throw new ResourceNotFoundException("User " + userId + " not found.");
        }
        return ResponseEntity.ok(notificationRepo.findAllByRecipient_IdOrderByCreatedAtDesc(userId).stream().map(notificationMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<NotificationResponse>> getUnreadNotificationsForUser(Long userId) {
        log.debug("Fetching unread notifications for user={}", userId);
        if (!userRepo.existsById(userId)) {
            throw new ResourceNotFoundException("User " + userId + " not found.");
        }
        return ResponseEntity.ok(notificationRepo.findAllByRecipient_IdAndReadFalseOrderByCreatedAtDesc(userId).stream().map(notificationMapper::toResponse).toList());
    }

    @Override
    @Transactional
    public ResponseEntity<NotificationResponse> markAsRead(Long notificationId) {
        log.info("Marking notification id={} as read", notificationId);
        Notification n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification " + notificationId + " not found."));
        n.setRead(true);
        return ResponseEntity.ok(notificationMapper.toResponse(notificationRepo.save(n)));
    }
}