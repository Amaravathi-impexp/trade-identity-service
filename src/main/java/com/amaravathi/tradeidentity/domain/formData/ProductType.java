package com.amaravathi.tradeidentity.domain.formData;


import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "product_type")
@Data
public class ProductType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SMALLINT")
    private int id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String category;

    private String description;

    @Column(name = "hs_code")
    private String hsCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
