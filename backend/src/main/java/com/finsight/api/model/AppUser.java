package com.finsight.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_auth0_sub", columnList = "auth0_sub", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The Auth0 `sub` claim (e.g. "auth0|abc123") – unique per tenant */
    @Column(name = "auth0_sub", nullable = false, unique = true, length = 60)
    private String auth0Sub;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
}

