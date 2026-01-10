package com.amaravathi.tradeidentity.api.auth.dto;

import java.util.UUID;
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
public class MeResponseDto {

    int id;
    String email;
    String fullName;
    List<String> roles;
    boolean emailVerified;
    boolean phoneVerified;
}
