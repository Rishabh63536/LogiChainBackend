package com.cts.logichain360.entity;

import com.cts.logichain360.enums.PaymentStatus;
import com.cts.logichain360.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@SQLDelete(sql = "UPDATE payments SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.SUCCESS;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String method = "DUMMY_GATEWAY";

    @Column(nullable = false)
    private LocalDateTime paidAt;
}