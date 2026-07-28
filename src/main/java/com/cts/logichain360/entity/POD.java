package com.cts.logichain360.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "pods")
@SQLDelete(sql = "UPDATE pods SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class POD extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Orders order;

    //Just the stored filename,full url built by mapper
    @Column(nullable = false, length = 255)
    private String photoFilename;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;
}