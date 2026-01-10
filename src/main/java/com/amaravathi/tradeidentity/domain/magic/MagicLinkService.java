package com.amaravathi.tradeidentity.domain.magic;

import com.amaravathi.tradeidentity.domain.user.AppUser;
import com.amaravathi.tradeidentity.domain.user.AppUserRepository;
import com.amaravathi.tradeidentity.domain.user.UserStatus;
import com.amaravathi.tradeidentity.util.TokenGenerator;
import com.amaravathi.tradeidentity.util.TokenHash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
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

    public void sendEmailVerifyLink(String email, String redirectUrl) {
        // Always pretend success to avoid enumeration
        userRepo.findByEmailIgnoreCase(email).ifPresent(user -> {
            String raw = TokenGenerator.opaqueToken();
            String hash = TokenHash.sha256(raw);

            MagicLinkToken t = new MagicLinkToken();
            t.setUserId(user.getId());
            t.setPurpose(MagicLinkPurpose.EMAIL_VERIFY);
            t.setTokenHash(hash);
            t.setExpiresAt(OffsetDateTime.now().plusMinutes(ttlMinutes));
            t.setRedirectUrl(redirectUrl);
            repo.save(t);

            String link = frontendBaseUrl + "/verify-email?token=" + raw;
            // TODO: replace with real email sender
            System.out.println("MAGIC LINK EMAIL TO " + email + " => " + link);
        });
    }

    public boolean confirmEmail(String rawToken) {
        String hash = TokenHash.sha256(rawToken);
        MagicLinkToken t = repo.findByTokenHash(hash).orElseThrow(() -> new IllegalArgumentException("Invalid/expired link"));
        if (!repo.isValid(t)) throw new IllegalArgumentException("Invalid/expired link");

        t.setUsedAt(OffsetDateTime.now());
        repo.save(t);

        AppUser user = userRepo.findById(t.getUserId()).orElseThrow();
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        return true;
    }
}
