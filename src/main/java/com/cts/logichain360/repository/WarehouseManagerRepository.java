package com.cts.logichain360.repository;

import com.cts.logichain360.entity.Driver;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.entity.WarehouseManager;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WarehouseManagerRepository extends JpaRepository<WarehouseManager, Long> {
    Optional<WarehouseManager> findByUser_Id(Long userId);
    Optional<WarehouseManager> findByUser(User user);
    Optional<WarehouseManager> findByAssignedWarehouse_Id(Long warehouseId);
}