package com.cts.logichain360.entity;

import java.time.LocalDateTime;

import com.cts.logichain360.enums.NotificationType;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "notifications")

@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE notifications SET is_deleted = true WHERE id = ?")
public class Notification extends SoftDeletableEntity{
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	//many notifications to one user
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipient_user_id", nullable =false)
	private User recipient;
	
	@Column(nullable = false , length= 500)
	private String message;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;
	
	 @Column(nullable = false)
	 private LocalDateTime createdAt;
	 
	 //by default read status is false
	 @Column(name="is_read", nullable = false)
	 @Builder.Default
	 private boolean read = false;
	 
	 private Long relatedEntityId;
	 private String relatedEntityType;
}
