package com.amaravathi.tradeidentity.api.formData.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeResponseDto {
    int id;
    String code;
    String name;
    String category;
    String hsCode;
}
