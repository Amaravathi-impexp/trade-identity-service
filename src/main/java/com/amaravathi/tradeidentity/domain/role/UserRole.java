package com.amaravathi.tradeidentity.domain.role;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_role")
@IdClass(UserRole.PK.class)
public class UserRole {

    @Id
    @Column(name = "user_id", columnDefinition = "INTEGER")
    private int userId;

    @Id
    @Column(name = "role_id", columnDefinition = "SMALLINT")
    private int roleId;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "assigned_by", columnDefinition = "INTEGER")
    private int assignedBy;

    @PrePersist
    void prePersist() {
        if (assignedAt == null) assignedAt = OffsetDateTime.now();
    }

    public UserRole() {}
    public UserRole(int userId, int roleId, int assignedBy) {
        this.userId = userId;
        this.roleId = roleId;
        this.assignedBy = assignedBy;
    }

    public UserRole(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public static class PK implements Serializable {
        public int userId;
        public int roleId;
    }
}
