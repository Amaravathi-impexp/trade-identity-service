package com.amaravathi.tradeidentity.domain.training;


import com.amaravathi.tradeidentity.domain.role.UserRole;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_training")
@IdClass(UserTraining.PK.class)
@Data
public class UserTraining {

    @Id
    @Column(name = "user_id", columnDefinition = "INTEGER")
    private int userId;

    @Id
    @Column(name = "training_id", columnDefinition = "SMALLINT")
    private int trainingId;


    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;


    @PrePersist
    void prePersist() {
        if (assignedAt == null) assignedAt = OffsetDateTime.now();
    }

    public static class PK implements Serializable {
        public int userId;
        public int trainingId;
    }
}
