package com.amaravathi.tradeidentity.api.admin;

import com.amaravathi.tradeidentity.api.admin.dto.CreateRoleRequestDto;
import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.api.admin.dto.UpdateRoleRequestDto;
import com.amaravathi.tradeidentity.domain.role.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/trade-identity/v1/admin/roles")
@Slf4j
@Validated
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<List<RoleResponseResponseDto>> getAllRoles() {
        log.info("GET /admin/roles");
        return ResponseEntity.status(HttpStatus.OK).body(roleService.getAllRoles());
    }

    @PostMapping
    public ResponseEntity<RoleResponseResponseDto>  create(@Valid @RequestBody CreateRoleRequestDto req) {
        log.info("POST /admin/roles code={} name={}", req.getCode(), req.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(req));
    }

    @PatchMapping("/{roleId}")
    public ResponseEntity<RoleResponseResponseDto> update(@PathVariable int roleId,
                                                          @Valid @RequestBody UpdateRoleRequestDto req) {
        log.info("PATCH /admin/roles/{} (update)", roleId);
        return ResponseEntity.status(HttpStatus.OK).body( roleService.updateRole(roleId, req));

    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<RoleResponseResponseDto> delete(@PathVariable int roleId) {
        log.info("DELETE /admin/roles/{}", roleId);
        return ResponseEntity.status(HttpStatus.OK).body(roleService.deleteRoleById(roleId));

    }
}
