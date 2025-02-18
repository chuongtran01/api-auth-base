package com.personal.api_auth_base.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "role")
@EntityListeners(AuditingEntityListener.class)

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId; // Primary key

    @Column(nullable = false, unique = true)
    private String name; // Role name, e.g., ROLE_ADMIN or ROLE_USER

    @Column(length = 255)
    private String description; // Optional: Description of the role

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt; // Timestamp when the role was created

    // Dont work if left un-commented
//    @ManyToMany(mappedBy = "roles") // Bidirectional mapping
//    @JsonBackReference
//    private Set<User> users = new HashSet<>();


}
