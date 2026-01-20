package com.amaravathi.tradeidentity.domain.magic;

import com.amaravathi.tradeidentity.common.ResourceNotFoundException;
import com.amaravathi.tradeidentity.common.TradeIdentityException;
import com.amaravathi.tradeidentity.domain.user.AppUser;
import com.amaravathi.tradeidentity.domain.user.AppUserRepository;
import com.amaravathi.tradeidentity.domain.user.UserStatus;
import com.amaravathi.tradeidentity.util.TokenGenerator;
import com.amaravathi.tradeidentity.util.TokenHash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class MagicLinkService {

    private final MagicLinkTokenRepository repo;
    private final AppUserRepository userRepo;

    private final String frontendBaseUrl;
    private final long ttlMinutes;

    public MagicLinkService(
            MagicLinkTokenRepository repo,
            AppUserRepository userRepo,
            @Value("${app.frontendBaseUrl}") String frontendBaseUrl,
            @Value("${security.magicLink.ttlMinutes}") long ttlMinutes
    ) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.frontendBaseUrl = frontendBaseUrl;
        this.ttlMinutes = ttlMinutes;
    }

    // ------------------ SEND VERIFY LINK ------------------

    @Transactional
    public void sendEmailVerifyLink(String email, String redirectUrl) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        log.info("Sending email verification link email={}", email);

        try {
            userRepo.findByEmailIgnoreCase(email.trim()).ifPresentOrElse(user -> {

                String raw = TokenGenerator.opaqueToken();
                String hash = TokenHash.sha256(raw);

                MagicLinkToken t = new MagicLinkToken();
                t.setUserId(user.getId());
                t.setPurpose(MagicLinkPurpose.EMAIL_VERIFY);
                t.setTokenHash(hash);
                t.setExpiresAt(OffsetDateTime.now().plusMinutes(ttlMinutes));
                t.setRedirectUrl(redirectUrl);

                repo.save(t);

                // Build link (do NOT log token in real environments)
                String link = frontendBaseUrl + "/verify-email?token=" + raw;

                // TODO: send email using actual email provider
                log.debug("Magic link generated for email={} userId={} ttlMinutes={}",
                        email, user.getId(), ttlMinutes);

                // If you really need to see it in dev, log masked:
                log.debug("MAGIC LINK (dev only) email={} link={}", email, maskTokenInUrl(link));

            }, () -> {
                // Your current behavior is silent no-op.
                // This is often preferred to avoid user enumeration.
                log.info("Email verification link requested for non-existing email={}", email);
            });

        } catch (DataIntegrityViolationException dive) {
            log.error("Integrity violation while creating magic link token email={}", email, dive);
            throw new TradeIdentityException("Failed to generate email verification link", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while creating magic link token email={}", email, dae);
            throw new TradeIdentityException("Database error while sending email verification link", dae);
        }
    }

    // ------------------ CONFIRM EMAIL ------------------

    @Transactional
    public boolean confirmEmail(String rawToken) {
        log.info("Confirming email verification link");

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Invalid/expired link");
        }

        try {
            String hash = TokenHash.sha256(rawToken);

            MagicLinkToken t = repo.findByTokenHash(hash)
                    // Security tip: you can throw IllegalArgumentException("Invalid/expired link") always
                    .orElseThrow(() -> new ResourceNotFoundException("Magic link token not found"));

            if (!repo.isValid(t)) {
                throw new IllegalArgumentException("Invalid/expired link");
            }

            // Mark token used
            t.setUsedAt(OffsetDateTime.now());
            repo.save(t);

            // Mark user verified
            AppUser user = userRepo.findById(t.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for magic link token"));

            user.setEmailVerified(true);
            user.setStatus(UserStatus.ACTIVE);
            userRepo.save(user);

            log.info("Email verified successfully userId={}", user.getId());
            return true;

        } catch (DataAccessException dae) {
            log.error("Database error while confirming email verification", dae);
            throw new TradeIdentityException("Database error while confirming email", dae);
        }
    }

    // ------------------ helper ------------------

    private String maskTokenInUrl(String url) {
        // masks token value if url contains token=....
        // Example: token=abcd1234 -> token=****1234
        int idx = url.indexOf("token=");
        if (idx < 0) return url;
        String prefix = url.substring(0, idx + "token=".length());
        String token = url.substring(idx + "token=".length());
        if (token.length() <= 4) return prefix + "****";
        return prefix + "****" + token.substring(token.length() - 4);
    }
}
