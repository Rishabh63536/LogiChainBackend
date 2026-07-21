package com.cts.logichain360.repository;

import com.cts.logichain360.dto.response.WarehouseCollectionRecordResponse;
import com.cts.logichain360.entity.Payment;
import com.cts.logichain360.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByOrder_IdOrderByPaidAtAsc(Long orderId);
    Optional<Payment> findByOrder_IdAndType(Long orderId, PaymentType type);
    boolean existsByOrder_IdAndType(Long orderId, PaymentType type);

    @Query("SELECT new com.cts.logichain360.dto.response.WarehouseCollectionRecordResponse(" +
            "w.id, w.warehouseCode, o.id, p.type, p.amount, p.paidAt) " +
            "FROM Payment p " +
            "JOIN p.order o " +
            "JOIN o.productWarehouse pw " +
            "JOIN pw.warehouse w " +
            "ORDER BY p.paidAt ASC")
    List<WarehouseCollectionRecordResponse> findAllWarehouseCollectionRecords();
}