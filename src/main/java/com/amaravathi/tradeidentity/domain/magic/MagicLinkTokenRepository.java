package com.amaravathi.tradeidentity.domain.magic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, Integer> {
    Optional<MagicLinkToken> findByTokenHash(String tokenHash);

    default boolean isValid(MagicLinkToken t) {
        return t.getUsedAt() == null && t.getExpiresAt().isAfter(OffsetDateTime.now());
    }
}
