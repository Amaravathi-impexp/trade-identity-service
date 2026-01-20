package com.amaravathi.tradeidentity.api.admin.dto;

import java.util.List;
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
public class UpdateUserRequestDto {
    Integer originCountryId;
    Integer destinationCountryId;
    Integer productTypeId;
    List<RoleResponseResponseDto> roles;
    boolean emailNotificationEnabled;
    boolean phoneNotificationEnabled;
    boolean appNotificationEnabled;
}
