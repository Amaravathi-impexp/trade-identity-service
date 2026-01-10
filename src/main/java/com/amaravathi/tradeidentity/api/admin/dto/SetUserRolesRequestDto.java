package com.amaravathi.tradeidentity.api.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
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
public class SetUserRolesRequestDto {
    @NotEmpty List<RoleResponseResponseDto> roles;
}
