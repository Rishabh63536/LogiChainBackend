package com.cts.logichain360.service;

import com.cts.logichain360.dto.response.NotificationResponse;
import com.cts.logichain360.entity.Orders;
import com.cts.logichain360.entity.ProductWarehouse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface NotificationService {

    //called by orderserviceimpl on stock below ROL
    //resolves the warehouse manager and creates a notification
	
    void notifyRolBreach(ProductWarehouse pw);

    //called by OrderServiceImpl.assignDriver() right after a driver is assigned,
    //notification would be given to assigned driver
    void notifyDriverAssigned(Orders order);

    //called by OrderServiceImpl at every status transition of order, to customer
    void notifyOrderStatusChanged(Orders order);

    ResponseEntity<List<NotificationResponse>> getNotificationsForUser(Long userId);
    ResponseEntity<List<NotificationResponse>> getUnreadNotificationsForUser(Long userId);
    ResponseEntity<NotificationResponse> markAsRead(Long notificationId);
}