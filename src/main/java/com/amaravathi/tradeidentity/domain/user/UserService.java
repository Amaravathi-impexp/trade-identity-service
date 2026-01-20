package com.amaravathi.tradeidentity.domain.user;

import com.amaravathi.tradeidentity.api.admin.dto.*;
import com.amaravathi.tradeidentity.api.auth.dto.SignUpRequestDto;
import com.amaravathi.tradeidentity.api.auth.dto.SignUpResponseDto;
import com.amaravathi.tradeidentity.common.ResourceNotFoundException;
import com.amaravathi.tradeidentity.common.TradeIdentityException;
import com.amaravathi.tradeidentity.domain.role.Role;
import com.amaravathi.tradeidentity.domain.role.RoleService;
import com.amaravathi.tradeidentity.domain.role.UserRoleRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.amaravathi.tradeidentity.domain.user.UserServiceUtil.*;

@Service
@Slf4j
public class UserService {
    private final AppUserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final EntityManager em;

    public UserService(AppUserRepository userRepo, PasswordEncoder passwordEncoder, UserRoleRepository userRoleRepo,
                       RoleService roleService, EntityManager em) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepo = userRoleRepo;
        this.roleService = roleService;
        this.em = em;
    }

    // ------------------ ADMIN CREATE USER ------------------

    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto req, Authentication auth) {
        log.info("Creating user (admin flow) email={}", req != null ? req.getEmail() : null);

        if (req == null) throw new IllegalArgumentException("Request cannot be null");

        try {
            if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
                log.warn("Email already exists email={}", req.getEmail());
                throw new IllegalArgumentException("Email already exists");
            }

            AppUser u = new AppUser();
            u.setEmail(req.getEmail().trim());
            u.setPhone(req.getPhone());
            u.setFullName(req.getFullName());
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            u.setStatus(UserStatus.ACTIVE);
            u.setEmailVerified(false);
            u.setPhoneVerified(false);

            u.setOriginCountryId(req.getOriginCountryId());
            u.setDestinationCountryId(req.getDestinationCountryId());
            u.setProductTypeId(req.getProductTypeId());

            u.setEmailNotificationEnabled(req.isEmailNotificationEnabled());
            u.setPhoneNotificationEnabled(req.isPhoneNotificationEnabled());
            u.setAppNotificationEnabled(req.isAppNotificationEnabled());

            u.setCity(req.getCity());
            u.setResidenceCountry(req.getResidenceCountry());
            u.setPreferredLanguage(req.getPreferredLanguage());
            u.setOccupation(req.getOccupation());
            u.setInterest(req.getInterest());
            u.setPreviousTradingExposure(req.getPreviousTradingExposure());
            u.setTermsAccepted(req.isTermsAccepted());
            u.setCommunicationConsent(req.isCommunicationConsent());

            AppUser saved = userRepo.save(u);
            log.info("User created userId={} email={}", saved.getId(), saved.getEmail());

            if (req.getRoles() != null && !req.getRoles().isEmpty()) {
                log.info("Assigning {} roles to new userId={}", req.getRoles().size(), saved.getId());
                setRoles(saved.getId(), req.getRoles(), auth);
            }

            return requireUser(saved.getId());

        } catch (DataIntegrityViolationException dive) {
            log.error("Data integrity violation while creating user email={}", req.getEmail(), dive);
            throw new TradeIdentityException("Invalid user data / duplicate constraints", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while creating user email={}", req.getEmail(), dae);
            throw new TradeIdentityException("Database error while creating user", dae);
        }
    }

    // ------------------ SIGNUP ------------------

    @Transactional
    public SignUpResponseDto signUpUser(SignUpRequestDto req) {
        log.info("Sign-up request received email={}", req != null ? req.getEmail() : null);

        if (req == null) throw new IllegalArgumentException("Request cannot be null");
        if (req.getEmail() == null || req.getEmail().isBlank()) throw new IllegalArgumentException("Email is required");
        if (req.getPassword() == null || req.getPassword().isBlank()) throw new IllegalArgumentException("Password is required");

        try {
            if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
                log.warn("Sign-up failed: email already exists email={}", req.getEmail());
                throw new IllegalArgumentException("Email already exists");
            }

            AppUser u = UserServiceUtil.convertRequestDtoToUserEntity(req, passwordEncoder);
            AppUser saved = userRepo.save(u);

            // Default role assignment is part of the same transaction
            roleService.createDefaultRole(saved.getId());

            log.info("Sign-up successful userId={} email={}", saved.getId(), saved.getEmail());

            return SignUpResponseDto.builder()
                    .message("Sign-up successful. Please login !!!")
                    .build();

        } catch (DataIntegrityViolationException dive) {
            log.error("Data integrity violation during sign-up email={}", req.getEmail(), dive);
            throw new TradeIdentityException("Invalid sign-up data / duplicate constraints", dive);

        } catch (DataAccessException dae) {
            log.error("Database error during sign-up email={}", req.getEmail(), dae);
            throw new TradeIdentityException("Database error during sign-up", dae);
        }
    }

    // ------------------ REQUIRE USER ------------------

    @Transactional(readOnly = true)
    public AppUser requireUserByEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");

        return userRepo.findByEmailWithDetails(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));
    }

    @Transactional(readOnly = true)
    public UserResponseDto requireUser(int userId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");

        AppUser user = userRepo.findByIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return UserResponseDto.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .originCountry(mapCountryEntityToCountryDto(user.getOriginCountry()))
                .destinationCountry(mapCountryEntityToCountryDto(user.getDestinationCountry()))
                .productType(mapProductTypeEntityToProductTypeDto(user.getProductType()))
                .roles(mapRoleEntityToRoleResponseDto(user.getRoles()))
                .isEmailNotificationEnabled(user.isEmailNotificationEnabled())
                .isAppNotificationEnabled(user.isAppNotificationEnabled())
                .isPhoneNotificationEnabled(user.isPhoneNotificationEnabled())
                .build();
    }

    // ------------------ GET ALL USERS ------------------

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");

        try {
            List<AppUser> users = userRepo.findAllWithDetails();
            log.debug("Found {} users", users.size());

            return users.stream()
                    .map(user ->  UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .city(user.getCity())
                            .emailVerified(user.isEmailVerified())
                            .status(user.getStatus())
                            .isEmailNotificationEnabled(user.isEmailNotificationEnabled())
                            .interest(user.getInterest())
                            .fullName(user.getFullName())
                            .occupation(user.getOccupation())
                            .previousTradingExposure(user.getPreviousTradingExposure())
                            .residenceCountry(user.getResidenceCountry())
                            .preferredLanguage(user.getPreferredLanguage())
                            .build()
                    ).toList();

        } catch (DataAccessException dae) {
            log.error("Database error while fetching users", dae);
            throw new TradeIdentityException("Database error while fetching users", dae);
        }
    }

    // ------------------ GET ROLES BY USER UUID ------------------

    @Transactional(readOnly = true)
    public List<RoleResponseResponseDto> getRolesByUserId(UUID userId) {
        log.info("Fetching roles for userUuid={}", userId);

        if (userId == null) throw new IllegalArgumentException("userId cannot be null");

        try {
            List<Role> userRoles = userRepo.findByIdWithRoles(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                    .getRoles();

            return userRoles.stream()
                    .map(role -> RoleResponseResponseDto.builder()
                            .id(role.getId())
                            .code(role.getCode())
                            .name(role.getName())
                            .description(role.getDescription())
                            .build())
                    .toList();

        } catch (DataAccessException dae) {
            log.error("Database error while fetching roles for userUuid={}", userId, dae);
            throw new TradeIdentityException("Database error while fetching user roles", dae);
        }
    }

    // ------------------ DELETE USER (CURRENT BEHAVIOR: delete roles mapping) ------------------

    @Transactional
    public void deleteUser(int userId) {
        log.info("Deleting user roles mapping for userId={}", userId);

        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");

        try {
            int deleted = userRoleRepo.deleteByUserId(userId); // recommend returning int
            log.info("Deleted {} role mappings for userId={}", deleted, userId);

            // If you actually want to delete the user entity too:
            // userRepo.deleteById(userId);

        } catch (DataIntegrityViolationException dive) {
            log.error("Integrity violation while deleting user role mappings userId={}", userId, dive);
            throw new TradeIdentityException("Cannot delete user roles due to constraints", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while deleting user role mappings userId={}", userId, dae);
            throw new TradeIdentityException("Database error while deleting user roles", dae);
        }
    }

    // ------------------ DISABLE USER ------------------

    @Transactional
    public String disableUser(int userId) {
        log.info("Disabling userId={}", userId);

        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");

        try {
            AppUser user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            user.setStatus(UserStatus.DISABLED);
            userRepo.save(user);

            log.info("User disabled userId={}", userId);
            return "Successfully Disabled User!!";

        } catch (DataAccessException dae) {
            log.error("Database error while disabling userId={}", userId, dae);
            throw new TradeIdentityException("Database error while disabling user", dae);
        }
    }

    // ------------------ UPDATE USER ------------------

    @Transactional
    public UserResponseDto updateUser(int userId, UpdateUserRequestDto req, Authentication auth) {
        log.info("Updating userId={}", userId);

        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");
        if (req == null) throw new IllegalArgumentException("Request cannot be null");

        try {
            AppUser u = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            u.setEmailNotificationEnabled(req.isEmailNotificationEnabled());
            u.setPhoneNotificationEnabled(req.isPhoneNotificationEnabled());
            u.setAppNotificationEnabled(req.isAppNotificationEnabled());

            if (req.getOriginCountryId() != null && req.getOriginCountryId() > 0) u.setOriginCountryId(req.getOriginCountryId());
            if (req.getDestinationCountryId() != null && req.getDestinationCountryId() > 0) u.setDestinationCountryId(req.getDestinationCountryId());
            if (req.getProductTypeId() != null && req.getProductTypeId() > 0) u.setProductTypeId(req.getProductTypeId());

            // Do you really want to force ACTIVE on every update?
            // Keep if that's your business rule:
            u.setStatus(UserStatus.ACTIVE);

            userRepo.save(u);

            if (req.getRoles() != null && !req.getRoles().isEmpty()) {
                log.info("Updating roles for userId={}, rolesCount={}", userId, req.getRoles().size());
                setRoles(userId, req.getRoles(), auth);
            }

            log.info("User updated userId={}", userId);
            return requireUser(userId);

        } catch (DataIntegrityViolationException dive) {
            log.error("Integrity violation while updating userId={}", userId, dive);
            throw new TradeIdentityException("User update violates constraints", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while updating userId={}", userId, dae);
            throw new TradeIdentityException("Database error while updating user", dae);
        }
    }

    // ------------------ CHANGE USER STATUS (FIXED TX) ------------------

    @Transactional
    public UserResponseDto changeUserStatus(int userId, ChangeUserStatusRequestDto req) {
        log.info("Changing user status userId={}", userId);

        if (userId <= 0) throw new IllegalArgumentException("Invalid userId");
        if (req == null || req.getStatus() == null) throw new IllegalArgumentException("Status is required");

        try {
            AppUser u = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            u.setStatus(req.getStatus());
            userRepo.save(u);

            log.info("User status updated userId={} status={}", userId, req.getStatus());

            return UserResponseDto.builder()
                    .occupation(u.getOccupation())
                    .city(u.getCity())
                    .status(u.getStatus())
                    .phone(u.getPhone())
                    .id(u.getId())
                    .interest(u.getInterest())
                    .isPhoneNotificationEnabled(u.isPhoneNotificationEnabled())
                    .isAppNotificationEnabled(u.isAppNotificationEnabled())
                    .emailVerified(u.isEmailVerified())
                    .phoneVerified(u.isPhoneVerified())
                    .residenceCountry(u.getResidenceCountry())
                    .email(u.getEmail())
                    .fullName(u.getFullName())
                    .isEmailNotificationEnabled(u.isEmailNotificationEnabled())
                    .build();

        } catch (DataAccessException dae) {
            log.error("Database error while changing status userId={}", userId, dae);
            throw new TradeIdentityException("Database error while changing user status", dae);
        }
    }

    public void  setRoles(int userId, List<RoleResponseResponseDto> roles, Authentication auth) {

        roleService.createUserRoles(userId, roles, auth);

    }
}
