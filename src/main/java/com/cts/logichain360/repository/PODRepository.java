package com.cts.logichain360.repository;

import com.cts.logichain360.entity.POD;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PODRepository extends JpaRepository<POD, Long> {
    Optional<POD> findByOrder_Id(Long orderId);
    boolean existsByOrder_Id(Long orderId);
}