package com.amaravathi.tradeidentity.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CreateRoleRequestDto {
    @NotBlank String code;
    @NotBlank String name;
    String description;
}
