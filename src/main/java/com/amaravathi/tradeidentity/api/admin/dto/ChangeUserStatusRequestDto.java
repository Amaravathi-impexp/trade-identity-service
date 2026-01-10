package com.amaravathi.tradeidentity.api.admin.dto;

import com.amaravathi.tradeidentity.domain.user.UserStatus;
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
public class ChangeUserStatusRequestDto {
    @NotNull UserStatus status;
}
