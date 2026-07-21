package com.cts.logichain360.mapper;

import org.springframework.stereotype.Component;

import com.cts.logichain360.dto.request.UserRegistrationRequest;
import com.cts.logichain360.dto.response.LoginResponse;
import com.cts.logichain360.dto.response.UserRegistrationResponse;
import com.cts.logichain360.dto.response.UserResponse;
import com.cts.logichain360.entity.Customer;
import com.cts.logichain360.entity.Driver;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.entity.Vendor;
import com.cts.logichain360.entity.WarehouseManager;

@Component
public class UserMapper {


    public User toUser(UserRegistrationRequest req, String encodedPassword) {
        return User.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .password(encodedPassword)
                .role(req.getRole())
                .build();
    }

    public Customer toCustomer(User user, UserRegistrationRequest req) {
        return Customer.builder()
                .user(user)
                .companyName(req.getCompanyName())
                .gstNumber(req.getGstNumber())
                .email(req.getEmail())
                .shippingAddress(req.getShippingAddress())
                .creditLimit(req.getCreditLimit())
                .paymentTerms(req.getPaymentTerms())
                .build();
    }

    public Vendor toVendor(User user, UserRegistrationRequest req) {
        return Vendor.builder()
                .user(user)
                .companyName(req.getCompanyName())
                .gstNumber(req.getGstNumber())
                .email(req.getEmail())
                .businessAddress(req.getBusinessAddress())
                .contactPerson(req.getContactPerson())
                .paymentTerms(req.getPaymentTerms())
                .build();
    }

    public WarehouseManager toWarehouseManager(User user, UserRegistrationRequest req) {
        return WarehouseManager.builder()
                .user(user)
                .employeeCode(req.getEmployeeCode())
                .designation(req.getDesignation())
                .build();
    }

    public Driver toDriver(User user, UserRegistrationRequest req) {
        return Driver.builder()
                .user(user)
                .licenseNumber(req.getLicenseNumber())
                .licenseExpiry(req.getLicenseExpiry())
                .location(req.getLocation())
                .build();
    }

    //entity -> response

    public UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .phone(u.getPhone())
                .role(u.getRole())
                .status(u.getStatus())
                .build();
    }

    public LoginResponse toLoginResponse(User u, String token, Long roleProfileId, String roleProfileTable, Long warehouseId) {
        return LoginResponse.builder()
                .token(token)
                .userId(u.getId())
                .name(u.getName())
                .phone(u.getPhone())
                .role(u.getRole())
                .roleProfileId(roleProfileId)       
                .roleProfileTable(roleProfileTable)
                .warehouseId(warehouseId)
                .build();
    }

    public UserRegistrationResponse toRegistrationResponse(User u, Long roleProfileId, String roleProfileTable) {
        return UserRegistrationResponse.builder()
                .userId(u.getId())
                .name(u.getName())
                .phone(u.getPhone())
                .role(u.getRole())
                .status(u.getStatus())
                .roleProfileId(roleProfileId)
                .roleProfileTable(roleProfileTable)
                .build();
    }
}