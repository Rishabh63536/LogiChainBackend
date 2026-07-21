package com.cts.logichain360.entity;

import com.cts.logichain360.enums.ReturnReason;
import com.cts.logichain360.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_requests")
@SQLDelete(sql = "UPDATE return_requests SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReturnRequest extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CHANGED: was @OneToOne with unique=true — an order could only ever have
    // ONE return request total. Now @ManyToOne so partial returns can happen
    // across multiple separate requests against the same order (e.g. return
    // 2 of 5 damaged now, another 1 later) — capped by returnQuantity totals,
    // not by a DB uniqueness constraint.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(nullable = false)
    private Integer returnQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnReason reason;

    @Column(length = 500)
    private String notes;

    //optional evidence photo
    private String photoFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    //when whm approves or rejects
    private LocalDateTime resolvedAt;
    private Long resolvedByManagerId;

    private Long pickupDriverId;

    //set during pickup and restocking
    private LocalDateTime restockedAt;

    private Double refundAmount;
    private Double handlingFeeAmount;
}