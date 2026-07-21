package com.cts.logichain360.dto.request;

import com.cts.logichain360.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "User role cannot be null")
    private UserRole role;

    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String companyName;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "Invalid GST Number format")
    private String gstNumber;

    @Email(message = "Invalid email format")
    private String email;

    // CUSTOMER specific
    private String shippingAddress;
    private String billingAddress;

    @Min(value = 0, message = "Credit limit cannot be negative")
    private Double creditLimit;

    private String paymentTerms;

    // VENDOR specific
    private String businessAddress;
    private String contactPerson;

    // WAREHOUSE_MANAGER specific
    private String employeeCode;
    private String designation;

    // DRIVER specific
    @Size(min = 5, max = 20, message = "License number must be between 5 and 20 characters")
    private String licenseNumber;
    private String location;

    @FutureOrPresent(message = "License expiry date must be in the present or future")
    private LocalDate licenseExpiry;

    private String city;

}