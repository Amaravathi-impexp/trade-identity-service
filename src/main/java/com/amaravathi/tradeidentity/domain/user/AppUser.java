package com.amaravathi.tradeidentity.domain.user;

import com.amaravathi.tradeidentity.domain.formData.Country;
import com.amaravathi.tradeidentity.domain.formData.ProductType;
import com.amaravathi.tradeidentity.domain.role.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "app_user")
@Data
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    private int id;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified;

    @Column(name = "origin_country_id", columnDefinition = "SMALLINT")
    private Integer originCountryId;

    @Column(name = "destination_country_id", columnDefinition = "SMALLINT")
    private Integer destinationCountryId;

    @Column(name = "product_type_id", columnDefinition = "SMALLINT")
    private Integer productTypeId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    @Column(name = "is_email_notification")
    private boolean emailNotificationEnabled;
    @Column(name = "is_phone_notification")
    private boolean phoneNotificationEnabled;
    @Column(name = "is_app_notification")
    private boolean appNotificationEnabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_country_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Country originCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_country_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Country destinationCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ProductType productType;

    @PrePersist
    void prePersist() {
        //if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (status == null) status = UserStatus.CREATED;
    }

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
