package com.cts.logichain360.repository;

import com.cts.logichain360.entity.User;
import com.cts.logichain360.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUser_Id(Long userId);
    Optional<Vendor> findByUser(User user);
}