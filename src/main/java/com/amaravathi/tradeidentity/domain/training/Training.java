package com.amaravathi.tradeidentity.domain.training;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Entity
@Table(name = "training")
@Data
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SMALLINT")
    private int id;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate firstSessionDate;
    private LocalTime firstSessionStartTime;
    private LocalTime firstSessionEndTime;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate secondSessionDate;
    private LocalTime secondSessionStartTime;
    private LocalTime secondSessionEndTime;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }
    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }

}
