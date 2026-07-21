package com.cts.logichain360.repository;

import com.cts.logichain360.entity.Driver;
import com.cts.logichain360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUser(User user);
    Optional<Driver> findByUser_Id(Long userId);
    List<Driver> findAllByAvailableTrue();
    List<Driver> findAllByAvailableTrueAndLocation(String location);
}
