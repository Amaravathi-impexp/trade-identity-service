package com.amaravathi.tradeidentity.api.auth.dto;

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
public class VerifyEmailResponseDto {
    String message;
    boolean emailVerified;
}
