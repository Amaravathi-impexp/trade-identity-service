package com.amaravathi.tradeidentity.domain.formData;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "country")
@Data
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SMALLINT")
    private int id;

    @Column(nullable = false)
    private String name;

    private String currency;

    @Column(nullable = false, length = 2)
    private String iso2;

    @Column(nullable = false, length = 3)
    private String iso3;

    @Column(name = "phone_code")
    private String phoneCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

}
