package com.cts.logichain360.repository;

import com.cts.logichain360.entity.Product;
import com.cts.logichain360.entity.Vendor;
import com.cts.logichain360.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByVendor(Vendor vendor);
    List<Product> findAllByVendor_Id(Long vendorId);

//    List<Product> findAllByWarehouse(Warehouse warehouse);
//    List<Product> findAllByWarehouse_Id(Long warehouseId);

    // Monopoly- a vendor cannot have two products with the same name
    Optional<Product> findByProductNameAndVendor_Id(String productName, Long vendorId);
    boolean existsByProductNameAndVendor_Id(String productName, Long vendorId);
}