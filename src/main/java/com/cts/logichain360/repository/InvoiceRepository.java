package com.cts.logichain360.repository;

import com.cts.logichain360.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrder_Id(Long orderId);

    List<Invoice> findAllByCustomerIdOrderByIssuedAtDesc(Long customerId);

    boolean existsByOrder_Id(Long orderId);
}
