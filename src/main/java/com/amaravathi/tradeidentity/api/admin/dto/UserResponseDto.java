package com.amaravathi.tradeidentity.api.admin.dto;

import com.amaravathi.tradeidentity.api.formData.dto.CountryResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.ProductTypeResponseDto;
import com.amaravathi.tradeidentity.domain.user.UserStatus;

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
public class UserResponseDto {
    int id;
    String email;
    String fullName;
    String phone;
    UserStatus status;
    boolean emailVerified;
    boolean phoneVerified;
    CountryResponseDto originCountry;
    CountryResponseDto destinationCountry;
    ProductTypeResponseDto productType;
    List<RoleResponseResponseDto> roles;
    boolean isEmailNotificationEnabled;
    boolean isPhoneNotificationEnabled;
    boolean isAppNotificationEnabled;
}
