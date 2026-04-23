package com.localissue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double latitude;

    private Double longitude;

    private String category;

    @Column(nullable = false)
    private String status;

    private String imageUrl;

    @Column(updatable = false)
    private String createdBy;         // Keycloak subject (UUID) — stable unique identifier

    @Column(updatable = false)
    private String createdByUsername; // Keycloak preferred_username — for display

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
