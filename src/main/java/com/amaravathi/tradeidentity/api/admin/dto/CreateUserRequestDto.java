package com.amaravathi.tradeidentity.api.admin.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
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
public class CreateUserRequestDto {

    @Email @NotBlank String email;
    String phone;
    @NotBlank String fullName;
    @NotBlank String password;
    @NotNull int originCountryId;
    @NotNull int destinationCountryId;
    @NotNull int productTypeId;
    List<RoleResponseResponseDto> roles;
    boolean emailNotificationEnabled;
    boolean phoneNotificationEnabled;
    boolean appNotificationEnabled;
    @NotNull private String residenceCountry;
    @NotNull private String city;
    private String preferredLanguage;
    private String occupation;
    private String interest;
    private String previousTradingExposure;
    @NotNull private boolean termsAccepted;
    @NotNull private boolean communicationConsent;
}
