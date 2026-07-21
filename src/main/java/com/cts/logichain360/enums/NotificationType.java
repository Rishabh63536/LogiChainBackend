package com.cts.logichain360.enums;

public enum NotificationType {
    ROL_BREACH,      //when stock goes below rol
    ORDER_ASSIGNED,  //driver notified when assigned to order
    ORDER_STATUS_CHANGED  //customer notified when assigned/in_transit, delivered, cancelled
}