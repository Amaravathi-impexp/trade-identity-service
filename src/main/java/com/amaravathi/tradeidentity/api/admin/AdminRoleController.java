package com.amaravathi.tradeidentity.api.admin;

import com.amaravathi.tradeidentity.api.admin.dto.CreateRoleRequestDto;
import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.api.admin.dto.UpdateRoleRequestDto;
import com.amaravathi.tradeidentity.domain.role.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/trade-identity/v1/admin/roles")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final RoleService roleService;

    public AdminRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseResponseDto>> getAllRoles() {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.getAllRoles());
    }

    @PostMapping
    public ResponseEntity<RoleResponseResponseDto>  create(@Valid @RequestBody CreateRoleRequestDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createUser(req));
    }

    @PatchMapping("/{roleId}")
    public ResponseEntity<RoleResponseResponseDto> update(@PathVariable int roleId, @Valid @RequestBody UpdateRoleRequestDto req) {

        return ResponseEntity.status(HttpStatus.OK).body( roleService.updateRole(roleId, req));

    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<RoleResponseResponseDto> delete(@PathVariable int roleId) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.deleteRoleById(roleId));

    }
}
