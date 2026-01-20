package com.amaravathi.tradeidentity.api.auth;

import com.amaravathi.tradeidentity.api.auth.dto.*;
import com.amaravathi.tradeidentity.domain.magic.MagicLinkService;
import com.amaravathi.tradeidentity.domain.refresh.RefreshTokenService;
import com.amaravathi.tradeidentity.domain.role.Role;
import com.amaravathi.tradeidentity.domain.role.RoleService;
import com.amaravathi.tradeidentity.domain.user.AppUser;
import com.amaravathi.tradeidentity.domain.user.UserService;
import com.amaravathi.tradeidentity.security.JwtTokenService;
import com.amaravathi.tradeidentity.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.amaravathi.tradeidentity.domain.user.UserServiceUtil.*;


@RestController
@RequestMapping("/api/trade-identity/v1")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final MagicLinkService magicLinkService;

    @PostMapping("/auth/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto req) {

        log.info("POST /auth/sign-up email={}", req.getEmail());
        SignUpResponseDto signUpResponseDto = userService.signUpUser(req);

        // Send magic link to verify email
        magicLinkService.sendEmailVerifyLink(req.getEmail(), null);

        return  ResponseEntity.status(HttpStatus.CREATED).body(signUpResponseDto);
    }

    @PostMapping("/auth/sign-in")
    public SignInResponseDto signIn(@Valid @RequestBody SignInRequestDto req) {

        log.info("POST /auth/sign-in email={}", req.getEmail());

        AppUser user = userService.requireUserByEmail(req.getEmail());
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        List<String> roleCodes =
                user.getRoles() == null ? List.of() :
                        user.getRoles().stream()
                                .filter(Objects::nonNull)
                                .map(Role::getCode)
                                .filter(Objects::nonNull)
                                .toList();
        String access = jwtTokenService.generateAccessToken(user.getId(), roleCodes);
        String refresh = refreshTokenService.issue(user.getId());

        TokenPairResponseDto tokenPairResponseDto =  TokenPairResponseDto.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresInSeconds(900)
                .build();

        return SignInResponseDto.builder()
                .tokenDetails(tokenPairResponseDto)
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .originCountry(mapCountryEntityToCountryDto(user.getOriginCountry()))
                .destinationCountry(mapCountryEntityToCountryDto(user.getDestinationCountry()))
                .productType(mapProductTypeEntityToProductTypeDto(user.getProductType()))
                .roles(mapRoleEntityToRoleResponseDto(user.getRoles()))
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .isAppNotificationEnabled(user.isAppNotificationEnabled())
                .isEmailNotificationEnabled(user.isEmailNotificationEnabled())
                .isPhoneNotificationEnabled(user.isPhoneNotificationEnabled())
                .countryCode(user.getCountryCode())
                .build();
    }

    @PostMapping("/auth/refresh")
    public TokenPairResponseDto refresh(@Valid @RequestBody RefreshRequestDto req) {
        log.info("POST /auth/refresh");
        int userId = refreshTokenService.validateAndGetUserId(req.getRefreshToken());
        List<String> roles = roleService.roleCodesForUser(userId);

        String access = jwtTokenService.generateAccessToken(userId, roles);
        String newRefresh = refreshTokenService.rotate(req.getRefreshToken(), userId);

        return TokenPairResponseDto.builder()
                .accessToken(access)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresInSeconds(900)
                .build();
    }

    @PostMapping("/auth/logout")
    public GenericMessageResponseDto logout(@Valid @RequestBody LogoutRequestDto req) {
        log.info("POST /auth/logout");
        boolean all = req.getAllSessions() != null && req.getAllSessions();
        if (!all) {
            refreshTokenService.revokeOne(req.getRefreshToken());
            return new GenericMessageResponseDto("Logged out.");
        }
        int userId = refreshTokenService.validateAndGetUserId(req.getRefreshToken());
        refreshTokenService.revokeAll(userId);
        return new GenericMessageResponseDto("Logged out from all sessions.");
    }

    @GetMapping("/auth/me")
    public MeResponseDto me(Authentication auth) {
        log.info("GET /auth/me");
        SecurityUser principal = (SecurityUser) auth.getPrincipal();
        int userId = principal.userId();
        var user = userService.requireUser(userId);
        List<String> roles = roleService.roleCodesForUser(userId);

        return MeResponseDto.builder()
                .id(userId)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .phoneVerified(user.isPhoneVerified())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    @PostMapping("/verify/email/send-magic-link")
    public GenericMessageResponseDto sendMagic(@Valid @RequestBody SendMagicLinkRequestDto req) {
        log.info("POST /verify/email/send-magic-link email={}", req.getEmail());
        magicLinkService.sendEmailVerifyLink(req.getEmail(), req.getRedirectUrl());
        return new GenericMessageResponseDto("If an account exists, a verification link has been sent.");
    }

    @GetMapping("/verify/email/confirm")
    public VerifyEmailResponseDto confirm(@RequestParam String token) {
        log.info("GET /verify/email/confirm");
        magicLinkService.confirmEmail(token);
        return new VerifyEmailResponseDto("Email verified successfully.", true);
    }
}
