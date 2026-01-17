package com.amaravathi.tradeidentity.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    @Email @NotBlank String email;
    @NotBlank String phone;
    @NotBlank String fullName;
    @NotBlank String password;
    @NotNull private String residenceCountry;
    @NotNull private String city;
    private String preferredLanguage;
    private String occupation;
    private String interest;
    private String previousTradingExposure;
    @NotNull private boolean termsAccepted;
    @NotNull private boolean communicationConsent;
}
