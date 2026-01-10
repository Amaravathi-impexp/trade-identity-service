package com.amaravathi.tradeidentity.domain.refresh;

import com.amaravathi.tradeidentity.util.TokenGenerator;
import com.amaravathi.tradeidentity.util.TokenHash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long ttlDays;

    public RefreshTokenService(RefreshTokenRepository repo, @Value("${security.refresh.ttlDays}") long ttlDays) {
        this.repo = repo;
        this.ttlDays = ttlDays;
    }

    public String issue(int userId) {
        String raw = TokenGenerator.opaqueToken();
        String hash = TokenHash.sha256(raw);

        RefreshToken t = new RefreshToken();
        t.setUserId(userId);
        t.setTokenHash(hash);
        t.setExpiresAt(OffsetDateTime.now().plusDays(ttlDays));
        repo.save(t);
        return raw;
    }

    public int validateAndGetUserId(String rawRefreshToken) {
        String hash = TokenHash.sha256(rawRefreshToken);
        RefreshToken t = repo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (!repo.isValid(t)) throw new IllegalArgumentException("Invalid refresh token");
        return t.getUserId();
    }

    public String rotate(String oldRawRefreshToken, int userId) {
        String oldHash = TokenHash.sha256(oldRawRefreshToken);

        RefreshToken old = repo.findByTokenHash(oldHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (!repo.isValid(old)) throw new IllegalArgumentException("Invalid refresh token");

        String newRaw = TokenGenerator.opaqueToken();
        String newHash = TokenHash.sha256(newRaw);

        old.setRevokedAt(OffsetDateTime.now());
        old.setReplacedByHash(newHash);
        repo.save(old);

        RefreshToken fresh = new RefreshToken();
        fresh.setUserId(userId);
        fresh.setTokenHash(newHash);
        fresh.setExpiresAt(OffsetDateTime.now().plusDays(ttlDays));
        repo.save(fresh);

        return newRaw;
    }

    public void revokeOne(String rawRefreshToken) {
        String hash = TokenHash.sha256(rawRefreshToken);
        repo.findByTokenHash(hash).ifPresent(t -> {
            t.setRevokedAt(OffsetDateTime.now());
            repo.save(t);
        });
    }

    public void revokeAll(int userId) {
        // simple approach: delete all tokens (or mark revoked)
        repo.findAll().stream()
                .filter(t -> t.getUserId() == userId)
                .forEach(t -> { t.setRevokedAt(OffsetDateTime.now()); repo.save(t); });
    }
}
