package com.amaravathi.tradeidentity.api.training.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
