package com.amaravathi.tradeidentity.api.admin;

import com.amaravathi.tradeidentity.api.admin.dto.*;
import com.amaravathi.tradeidentity.domain.role.RoleService;
import com.amaravathi.tradeidentity.domain.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trade-identity/v1/admin/users")
@Slf4j
@Validated
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("GET /admin/users");
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody CreateUserRequestDto req, Authentication auth) {
        log.info("POST /admin/users email={} fullName={}", req.getEmail(), req.getFullName());
        var userResponseDto = userService.createUser(req, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable int userId) {
        log.info("GET /admin/users/{}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(userService.requireUser(userId));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDto> update(@PathVariable int userId,
                                                  @Valid @RequestBody UpdateUserRequestDto req,
                                                  Authentication auth) {
        log.info("PATCH /admin/users/{}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(userId, req, auth));
    }

    @PostMapping("/{userId}/status")
    public ResponseEntity<UserResponseDto> changeUserStatus(@PathVariable int userId,
                                                            @Valid @RequestBody ChangeUserStatusRequestDto req) {
        log.info("POST /admin/users/{}/status status={}", userId, req.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(userService.changeUserStatus(userId, req));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> disableUser(@PathVariable int userId) {
        log.info("DELETE /admin/users/{} (disable)", userId);
        String status = userService.disableUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserResponseDto> replaceRoles(@PathVariable int userId,
                                                        @Valid @RequestBody SetUserRolesRequestDto req,
                                     Authentication auth) {
        log.info("PUT /admin/users/{}/roles rolesCount={}", userId,
                req.getRoles() == null ? 0 : req.getRoles().size());
        userService.setRoles(userId, req.getRoles(), auth);
        return getUserById(userId);
    }


}
