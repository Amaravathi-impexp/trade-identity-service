package com.amaravathi.tradeidentity.api.training.dto;


import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequestDto {
    private SessionDto firstSession;
    private SessionDto secondSession;
}
