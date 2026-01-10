package com.amaravathi.tradeidentity.domain.user;

import com.amaravathi.tradeidentity.api.admin.dto.*;
import com.amaravathi.tradeidentity.api.auth.dto.SignUpRequestDto;
import com.amaravathi.tradeidentity.domain.role.Role;
import com.amaravathi.tradeidentity.domain.role.RoleService;
import com.amaravathi.tradeidentity.domain.role.UserRoleRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.amaravathi.tradeidentity.domain.user.UserServiceUtil.*;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
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

    public UserResponseDto createUser(CreateUserRequestDto req, Authentication auth) {
        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppUser u = new AppUser();
        u.setEmail(req.getEmail());
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
        AppUser user =  userRepo.save(u);

        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            setRoles(user.getId(), req.getRoles(), auth);
        }

        return this.requireUser(user.getId());

    }

    @Transactional
    public AppUser createUser(SignUpRequestDto req) {
        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        AppUser u = new AppUser();
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setFullName(req.getFullName());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setStatus(UserStatus.CREATED);
        return userRepo.save(u);
    }

    public AppUser requireUserByEmail(String email) {
        return userRepo.findByEmailWithDetails(email).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    }

    @Transactional(readOnly = true)
    public UserResponseDto requireUser(int userId) {
        AppUser user =  userRepo.findByIdWithDetails(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

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

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {

        return userRepo.findAllWithDetails().stream().map(user -> new UserResponseDto(
                user.getId(), user.getEmail(), user.getFullName(), user.getPhone(), user.getStatus(),
                user.isEmailVerified(), user.isPhoneVerified(),
                mapCountryEntityToCountryDto(user.getOriginCountry()),
                mapCountryEntityToCountryDto(user.getDestinationCountry()),
                mapProductTypeEntityToProductTypeDto(user.getProductType()),
                mapRoleEntityToRoleResponseDto(user.getRoles()),
                user.isEmailNotificationEnabled(),
                user.isPhoneNotificationEnabled(),
                user.isAppNotificationEnabled()
        )).toList();
    }

    public List<RoleResponseResponseDto> getRolesByUserId(UUID userId) {
        List<Role> userRoles = userRepo.findByIdWithRoles(userId)
                .orElseThrow()
                .getRoles();

        return userRoles.stream()
                        .map(role -> RoleResponseResponseDto.builder()
                                .id(role.getId())
                                .code(role.getCode())
                                .name(role.getName())
                                .description(role.getDescription())
                                .build()
                        )
                        .toList();
    }

    public void deleteUser(int userId) {
        userRoleRepo.deleteByUserId(userId);
    }

    public String disableUser(int userId) {
        AppUser user =  userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setStatus(com.amaravathi.tradeidentity.domain.user.UserStatus.DISABLED);
        userRepo.save(user);
        return "Successfully Disabled User!!";
    }

    public UserResponseDto updateUser(int userId, UpdateUserRequestDto req, Authentication auth) {
        AppUser u =  userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        u.setEmailNotificationEnabled(req.isEmailNotificationEnabled());
        u.setPhoneNotificationEnabled(req.isPhoneNotificationEnabled());
        u.setAppNotificationEnabled(req.isAppNotificationEnabled());
        if (req.getOriginCountryId() >= 0) u.setOriginCountryId(req.getOriginCountryId());
        if (req.getDestinationCountryId()  >= 0) u.setDestinationCountryId(req.getDestinationCountryId());
        if (req.getProductTypeId()  >= 0) u.setProductTypeId(req.getProductTypeId());
        u.setStatus(UserStatus.ACTIVE);
        AppUser user = userRepo.save(u);

        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            setRoles(user.getId(), req.getRoles(), auth);
        }

        return this.requireUser(userId);
    }

    @Transactional(readOnly = true)
    public UserResponseDto changeUserStatus(int userId, ChangeUserStatusRequestDto req) {
        AppUser u =  userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        u.setStatus(req.getStatus());
        userRepo.save(u);

        return new UserResponseDto(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getStatus(),
                u.isEmailVerified(), u.isPhoneVerified(),
                mapCountryEntityToCountryDto(u.getOriginCountry()),
                mapCountryEntityToCountryDto(u.getDestinationCountry()),
                mapProductTypeEntityToProductTypeDto(u.getProductType()),
                mapRoleEntityToRoleResponseDto(u.getRoles()),
                u.isEmailNotificationEnabled(), u.isPhoneNotificationEnabled(), u.isAppNotificationEnabled());

    }

    public void  setRoles(int userId, List<RoleResponseResponseDto> roles, Authentication auth) {
        //userService.deleteUser(userId);

        roleService.createUserRoles(userId, roles, auth);

    }
}
