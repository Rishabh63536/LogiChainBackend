
package com.cts.logichain360.entity;

import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouse_managers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE warehouse_managers SET is_deleted = true WHERE id = ?")
public class WarehouseManager extends SoftDeletableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(unique = true)
    private String employeeCode;

    private String designation;

    // One warehouse one manager
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_warehouse_id", unique = true)
    private Warehouse assignedWarehouse;
}