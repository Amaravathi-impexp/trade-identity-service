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
public class CountryResponseDto {
    int id;
    String iso2;
    String iso3;
    String name;
    String phoneCode;
    String currency;
}
