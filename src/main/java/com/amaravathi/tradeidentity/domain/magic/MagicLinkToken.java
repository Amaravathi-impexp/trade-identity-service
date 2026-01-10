package com.amaravathi.tradeidentity.domain.magic;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "magic_link_token")
@Data
public class MagicLinkToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    private int id;

    @Column(name = "user_id", nullable = false, columnDefinition = "INTEGER")
    private int userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MagicLinkPurpose purpose;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (purpose == null) purpose = MagicLinkPurpose.EMAIL_VERIFY;
    }
}
