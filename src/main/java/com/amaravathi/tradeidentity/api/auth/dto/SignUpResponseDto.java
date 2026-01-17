package com.amaravathi.tradeidentity.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignUpResponseDto {

    String message;
    String fullName;
    String email;
    String phone;
    private String residenceCountry;
    private String city;
    private String preferredLanguage;
    private String occupation;
    private String interest;
    private String previousTradingExposure;
}
