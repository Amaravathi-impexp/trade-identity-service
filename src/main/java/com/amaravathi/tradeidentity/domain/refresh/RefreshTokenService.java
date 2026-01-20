package com.amaravathi.tradeidentity.domain.refresh;

import com.amaravathi.tradeidentity.common.ResourceNotFoundException;
import com.amaravathi.tradeidentity.common.TradeIdentityException;
import com.amaravathi.tradeidentity.util.TokenGenerator;
import com.amaravathi.tradeidentity.util.TokenHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long ttlDays;

    public RefreshTokenService(
            RefreshTokenRepository repo,
            @Value("${security.refresh.ttlDays}") long ttlDays
    ) {
        this.repo = repo;
        this.ttlDays = ttlDays;
    }

    // ------------------ ISSUE ------------------

    @Transactional
    public String issue(int userId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");

        log.info("Issuing refresh token userId={}", userId);

        try {
            String raw = TokenGenerator.opaqueToken();
            String hash = TokenHash.sha256(raw);

            RefreshToken t = new RefreshToken();
            t.setUserId(userId);
            t.setTokenHash(hash);
            t.setExpiresAt(OffsetDateTime.now().plusDays(ttlDays));

            repo.save(t);

            log.info("Refresh token issued userId={} expiresInDays={}", userId, ttlDays);
            return raw;

        } catch (DataIntegrityViolationException dive) {
            // extremely rare: tokenHash unique constraint collision
            log.error("Integrity violation while issuing refresh token userId={}", userId, dive);
            throw new TradeIdentityException("Failed to issue refresh token due to constraint violation", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while issuing refresh token userId={}", userId, dae);
            throw new TradeIdentityException("Database error while issuing refresh token", dae);
        }
    }

    // ------------------ VALIDATE ------------------

    @Transactional(readOnly = true)
    public int validateAndGetUserId(String rawRefreshToken) {
        log.debug("Validating refresh token");

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        try {
            String hash = TokenHash.sha256(rawRefreshToken);

            RefreshToken t = repo.findByTokenHash(hash)
                    // For security you may prefer: new IllegalArgumentException("Invalid refresh token")
                    .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

            // prefer entity method t.isValid(now) if possible
            if (!repo.isValid(t)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            return t.getUserId();

        } catch (DataAccessException dae) {
            log.error("Database error while validating refresh token", dae);
            throw new TradeIdentityException("Database error while validating refresh token", dae);
        }
    }

    // ------------------ ROTATE (atomic) ------------------

    @Transactional
    public String rotate(String oldRawRefreshToken, int userId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");
        if (oldRawRefreshToken == null || oldRawRefreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        log.info("Rotating refresh token userId={}", userId);

        try {
            String oldHash = TokenHash.sha256(oldRawRefreshToken);

            RefreshToken old = repo.findByTokenHash(oldHash)
                    .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

            if (!repo.isValid(old) || old.getUserId() != userId) {
                // Important: also verify token belongs to the user
                throw new IllegalArgumentException("Invalid refresh token");
            }

            String newRaw = TokenGenerator.opaqueToken();
            String newHash = TokenHash.sha256(newRaw);

            // revoke old + link replacement
            old.setRevokedAt(OffsetDateTime.now());
            old.setReplacedByHash(newHash);
            repo.save(old);

            // create new token
            RefreshToken fresh = new RefreshToken();
            fresh.setUserId(userId);
            fresh.setTokenHash(newHash);
            fresh.setExpiresAt(OffsetDateTime.now().plusDays(ttlDays));
            repo.save(fresh);

            log.info("Refresh token rotated userId={} ttlDays={}", userId, ttlDays);
            return newRaw;

        } catch (DataIntegrityViolationException dive) {
            log.error("Integrity violation while rotating refresh token userId={}", userId, dive);
            throw new TradeIdentityException("Failed to rotate refresh token due to constraint violation", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while rotating refresh token userId={}", userId, dae);
            throw new TradeIdentityException("Database error while rotating refresh token", dae);
        }
    }

    // ------------------ REVOKE ONE ------------------

    @Transactional
    public void revokeOne(String rawRefreshToken) {
        log.debug("Revoking one refresh token");

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        try {
            String hash = TokenHash.sha256(rawRefreshToken);

            repo.findByTokenHash(hash).ifPresent(t -> {
                if (t.getRevokedAt() == null) {
                    t.setRevokedAt(OffsetDateTime.now());
                    repo.save(t);
                }
            });

        } catch (DataAccessException dae) {
            log.error("Database error while revoking refresh token", dae);
            throw new TradeIdentityException("Database error while revoking refresh token", dae);
        }
    }

    // ------------------ REVOKE ALL (efficient) ------------------

    @Transactional
    public int revokeAll(int userId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");

        log.info("Revoking all refresh tokens userId={}", userId);

        try {
            // Best: one DB update query
            int updated = repo.revokeAllByUserId(userId, OffsetDateTime.now());
            log.info("Revoked {} refresh tokens userId={}", updated, userId);
            return updated;

        } catch (DataAccessException dae) {
            log.error("Database error while revoking all refresh tokens userId={}", userId, dae);
            throw new TradeIdentityException("Database error while revoking refresh tokens", dae);
        }
    }
}