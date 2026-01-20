package com.amaravathi.tradeidentity.api.auth.dto;

import com.amaravathi.tradeidentity.api.formData.dto.CountryResponseDto;
import com.amaravathi.tradeidentity.api.formData.dto.ProductTypeResponseDto;
import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.domain.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInResponseDto {
    int id;
    String fullName;
    String email;
    String phone;
    UserStatus status;
    boolean emailVerified;
    boolean phoneVerified;
    CountryResponseDto originCountry;
    CountryResponseDto destinationCountry;
    ProductTypeResponseDto productType;
    List<RoleResponseResponseDto> roles;
    TokenPairResponseDto tokenDetails;
    private boolean isEmailNotificationEnabled;
    private boolean isPhoneNotificationEnabled;
    private boolean isAppNotificationEnabled;
    String countryCode;
}
