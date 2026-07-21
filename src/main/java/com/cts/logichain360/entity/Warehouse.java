package com.cts.logichain360.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name= "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE warehouses SET is_deleted = true WHERE id = ?")
public class Warehouse extends SoftDeletableEntity{
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable= false , unique = true)
	private String warehouseCode;
	
	@Column(nullable = false)
    private String location;
 
    private Integer capacity;
}
