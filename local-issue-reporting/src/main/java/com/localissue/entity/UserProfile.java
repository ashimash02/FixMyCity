package com.localissue.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    private String userId;      // Keycloak subject — no generated value, we own the ID

    @Column(nullable = false)
    private String username;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;
}
