package com.amaravathi.tradeidentity.api.admin.dto;

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
public class RoleResponseResponseDto {
    int id;
    String code;
    String name;
    String type;
    String description;
}
