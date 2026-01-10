package com.amaravathi.tradeidentity.domain.refresh;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    long deleteByUserId(int userId);
    long deleteByUserIdAndTokenHash(int userId, String tokenHash);
    // you can also add query for "valid token" if needed
    default boolean isValid(RefreshToken t) {
        return t.getRevokedAt() == null && t.getExpiresAt().isAfter(OffsetDateTime.now());
    }
}
