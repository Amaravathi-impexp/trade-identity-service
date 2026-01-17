package com.amaravathi.tradeidentity.domain.role;

import com.amaravathi.tradeidentity.api.admin.dto.CreateRoleRequestDto;
import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.api.admin.dto.UpdateRoleRequestDto;
import com.amaravathi.tradeidentity.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoleService {
    private final RoleRepository roleRepo;
    private final UserRoleRepository userRoleRepo;

    public RoleService(RoleRepository roleRepo, UserRoleRepository userRoleRepo) {
        this.roleRepo = roleRepo;
        this.userRoleRepo = userRoleRepo;
    }

    public List<String> roleCodesForUser(int userId) {
        return userRoleRepo.findRoleCodesByUserId(userId);
    }

    public List<RoleResponseResponseDto> getAllRoles() {
        return roleRepo.findAll().stream()
                .map(r -> new RoleResponseResponseDto(r.getId(), r.getCode(), r.getName(), r.getType(), r.getDescription()))
                .toList();

    }

    public RoleResponseResponseDto createUser(CreateRoleRequestDto req) {
        Role role = new Role();
        role.setCode(req.getCode());
        role.setName(req.getName());
        role.setDescription(req.getDescription());
        role.setType("CUSTOM");
        roleRepo.save(role);

       return new RoleResponseResponseDto(role.getId(), role.getCode(), role.getName(), role.getType(), role.getDescription());
    }

    public RoleResponseResponseDto deleteRoleById(int roleId) {
        roleRepo.deleteById(roleId);
        return new RoleResponseResponseDto(roleId, null, null, null, null);
    }

    public RoleResponseResponseDto updateRole(int roleId, UpdateRoleRequestDto roleReq) {
        Role role = roleRepo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        if (roleReq.getName() != null) role.setName(roleReq.getName());
        if (roleReq.getName() != null) role.setCode(roleReq.getName());
        if (roleReq.getDescription() != null) role.setDescription(roleReq.getDescription());
        roleRepo.save(role);
        return new RoleResponseResponseDto(role.getId(), role.getCode(), role.getName(), role.getType(), role.getDescription());
    }

    public void createUserRoles(int userId, List<RoleResponseResponseDto> roles, Authentication auth) {
        int assignedBy=-1;
        if (auth != null && auth.getPrincipal() instanceof SecurityUser su) {
            assignedBy = su.userId();
        }

        userRoleRepo.deleteByUserId(userId);

        for (RoleResponseResponseDto responseDto : roles) {
            var role = roleRepo.findById(responseDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + responseDto.getCode()));
            userRoleRepo.save(new UserRole(userId, role.getId(), assignedBy));
        }
    }

    public List<String> getAllRolesByUserId(int id) {
       return userRoleRepo.findRoleCodesByUserId(id);
    }

    public void createDefaultRole(int id) {


        Optional<Role> roleOptional = roleRepo.findByCode("ROLE_TRADER");
        if (roleOptional.isPresent()){
            UserRole userRole = new UserRole(id, roleOptional.get().getId(), id);
            userRoleRepo.save(userRole);
        } else {
            throw new IllegalArgumentException("Default Role not found");
        }
    }
}
