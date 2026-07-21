package com.cts.logichain360.repository;

import com.cts.logichain360.entity.Customer;
import com.cts.logichain360.entity.Driver;
import com.cts.logichain360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser_Id(Long userId);
    Optional<Customer> findByUser(User user);
}