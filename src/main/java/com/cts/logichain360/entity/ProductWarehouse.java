package com.cts.logichain360.entity;

import java.beans.Transient;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Entity
@Table(name = "product_warehouses",
    // 1:1 for now : a product can only be launched at one warehouse.
    // If we later scale to many to many then drop this constraint
    uniqueConstraints = @UniqueConstraint(columnNames = "product_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
public class ProductWarehouse extends SoftDeletableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull(message = "Stock cannot be null")
    @PositiveOrZero(message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;


    @NotNull(message = "Max stock cannot be null")
    @Positive(message = "Max stock must be greater than 0")
    @Column(nullable = false)
    private Integer maxStock;

    // ROL as percentage of maxStock. e.g. 40 means restock when stock < 40% of maxStock.

    @NotNull(message = "ROL percent cannot be null")
    @Min(value = 0, message = "ROL percent cannot be negative")
    @Max(value = 100, message = "ROL percent cannot exceed 100")
    @Column(name = "rol_percent", nullable = false)
    private Double rolPercent;

    //Derived: true when current stock is below the reorder level. Not persisted.
    @Transient
    public boolean isBelowRol() {
        if (maxStock == null || maxStock == 0 || rolPercent == null) return false;
        double stockPercent = (stock.doubleValue() / maxStock.doubleValue()) * 100.0;
        return stockPercent < rolPercent;
    }
}