package com.cts.logichain360.repository;

import com.cts.logichain360.entity.Orders;
import com.cts.logichain360.entity.ReturnRequest;
import com.cts.logichain360.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    Optional<ReturnRequest> findByOrder_Id(Long orderId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(rr.returnQuantity), 0) FROM ReturnRequest rr " +
                    "WHERE rr.order.id = :orderId AND rr.status <> 'REJECTED'"
    )
    Integer sumReturnedQuantityByOrderId(Long orderId);

    List<ReturnRequest> findAllByOrder_Customer_IdOrderByRequestedAtDesc(Long customerId);
    List<ReturnRequest> findAllByStatusOrderByRequestedAtAsc(ReturnStatus status);
    List<ReturnRequest> findAllByPickupDriverIdAndStatusOrderByResolvedAtAsc(Long driverId, ReturnStatus status);
}