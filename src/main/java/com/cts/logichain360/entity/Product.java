package com.cts.logichain360.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id = ?")
public class Product extends SoftDeletableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Min(value = 1, message = "Price cannot be negative")
    @Column(nullable = false)
    private Double productPrice;

    @Column(length = 1000)
    private String productDescription;

    //Many products belong to 1 vendor- vendor monopoly
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
}