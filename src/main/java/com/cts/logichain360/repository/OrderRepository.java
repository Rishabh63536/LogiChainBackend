package com.cts.logichain360.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.logichain360.entity.Orders;
import com.cts.logichain360.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Orders, Long>{
	List<Orders> findAllByCustomer_IdOrderByPlacedAtDesc(Long customerId);
    List<Orders> findAllByCustomer_IdAndStatusInOrderByPlacedAtDesc(Long customerId, Collection<OrderStatus> statuses);
    List<Orders> findAllByDriver_IdOrderByPlacedAtDesc(Long driverId);
    
    List<Orders> findAllByProductWarehouse_Warehouse_IdOrderByPlacedAtDesc(Long warehouseId);
    List<Orders> findAllByProductWarehouse_Warehouse_IdAndStatusOrderByPlacedAtAsc(Long warehouseId, OrderStatus status);
    List<Orders> findAll();
}