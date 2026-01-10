package com.amaravathi.tradeidentity.domain.formData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductTypeRepository extends JpaRepository<ProductType, Integer> {
}
