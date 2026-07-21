package com.cts.logichain360.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.cts.logichain360.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE orders SET is_deleted = true WHERE id = ?")
public class Orders extends SoftDeletableEntity {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
	
	
	//mapping many products to one customer
	@ManyToOne(fetch = FetchType.LAZY , optional= false)
	@JoinColumn(name = "customer_id", nullable =false)
	private Customer customer;
	
	//mapping many orders to one product
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	 @JoinColumn(name = "product_id", nullable = false)
	 private Product product;
	
	//needed to restock in case of order cancellation
	@ManyToOne(fetch= FetchType.LAZY, optional = false)
	@JoinColumn(name = "productWarehouse_id", nullable = false)
	private ProductWarehouse productWarehouse;
	
	//many orders to single driver 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name= "driver_id")
	private Driver driver;
	
	//snapshots at ordertime to survive price changes and soft-delete
	 @Column(nullable = false)
	 private String productNameSnapshot;
	 
	 @Column(nullable = false)
    private Double unitPriceSnapshot;
 
    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private Double amountPaid = 0.0;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
 
    @Column(nullable = false)
    private LocalDateTime placedAt;
 
    @Column(nullable = false, length = 500)
    private String shippingAddress;
    
    @Column(nullable = false)
	private Integer quantity;

    // True once amountPaid covers totalAmount both advance n final
    @Transient
    public boolean isFullyPaid() {
        if (totalAmount == null || amountPaid == null) return false;
        return amountPaid >= totalAmount - 0.01; // for floating point rounding
    }
}