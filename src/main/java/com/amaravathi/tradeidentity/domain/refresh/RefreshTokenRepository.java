package com.amaravathi.tradeidentity.domain.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Modifying
    @Query("""
    update RefreshToken t
       set t.revokedAt = :now
     where t.userId = :userId
       and t.revokedAt is null
""")
    int revokeAllByUserId(@Param("userId") int userId, @Param("now") OffsetDateTime now);

}
