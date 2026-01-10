package com.amaravathi.tradeidentity.api.auth.dto;

import jakarta.validation.constraints.Email;
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
public class SignInRequestDto {
    @Email @NotBlank String email;
    @NotBlank String password;
}
