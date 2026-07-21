package com.cts.logichain360.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE vendors SET is_deleted = true WHERE id = ?")
public class Vendor extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String companyName;

    @NotBlank(message = "GST Number is required")
    @Column(nullable = false, unique = true, length = 15)
    private String gstNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, length = 100)
    private String email;

    @NotBlank(message = "Business address is required")
    @Size(max = 255, message = "Business address cannot exceed 255 characters")
    @Column(length = 255)
    private String businessAddress;

    @NotBlank(message = "Contact person name is required")
    @Size(min = 2, max = 100, message = "Contact person name must be between 2 and 100 characters")
    @Column(length = 100)
    private String contactPerson;

    @Size(max = 100, message = "Payment terms cannot exceed 100 characters")
    @Column(length = 100)
    private String paymentTerms;
}