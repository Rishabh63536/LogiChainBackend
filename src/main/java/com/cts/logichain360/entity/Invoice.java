package com.cts.logichain360.entity;

import com.cts.logichain360.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE notifications SET is_deleted = true WHERE id = ?")
public class Invoice extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Orders order;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerCompany;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private String vendorCompanyName;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    @Builder.Default
    private Double taxPercent = 18.0;

    @Column(nullable = false)
    private Double taxAmount;

    @Column(nullable = false)
    @Builder.Default
    private Double deliveryFee = 0.0;


    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.ACTIVE;

    private LocalDateTime voidedAt;
}
