package com.cts.logichain360.entity;

import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// Overriding jpa delete to set field as true false
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
// automatically will filter all queries where isDeleted is false
@Where(clause = "is_deleted = false")

public class User extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{9,14}$", message = "Invalid phone number format")
    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull(message = "User role is required")
    @Enumerated(EnumType.STRING)
    // Limiting enum string length in the DB saves space
    @Column(nullable = false, length = 30)
    private UserRole role;

    @NotNull(message = "User status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

}