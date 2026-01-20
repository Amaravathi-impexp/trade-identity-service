package com.amaravathi.tradeidentity.domain.role;

import com.amaravathi.tradeidentity.api.admin.dto.CreateRoleRequestDto;
import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.api.admin.dto.UpdateRoleRequestDto;
import com.amaravathi.tradeidentity.common.ResourceNotFoundException;
import com.amaravathi.tradeidentity.common.TradeIdentityException;
import com.amaravathi.tradeidentity.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepo;
    private final UserRoleRepository userRoleRepo;

    // ------------------ READ METHODS ------------------

    @Transactional(readOnly = true)
    public List<String> roleCodesForUser(int userId) {
        log.info("Fetching role codes for userId={}", userId);

        if (userId <= 0) {
            log.warn("Invalid userId={}", userId);
            throw new IllegalArgumentException("Invalid userId");
        }

        try {
            List<String> codes = userRoleRepo.findRoleCodesByUserId(userId);
            log.debug("Found {} role codes for userId={}", (codes == null ? 0 : codes.size()), userId);
            return (codes == null) ? List.of() : codes;
        } catch (DataAccessException dae) {
            log.error("Database error while fetching role codes for userId={}", userId, dae);
            throw new TradeIdentityException("Database error while fetching role codes", dae);
        }
    }

    @Transactional(readOnly = true)
    public List<RoleResponseResponseDto> getAllRoles() {
        log.info("Fetching all roles");

        try {
            List<Role> roles = roleRepo.findAll();
            log.debug("Found {} roles", roles.size());

            return roles.stream()
                    .map(r -> RoleResponseResponseDto.builder()
                            .id(r.getId())
                            .name(r.getName())
                            .code(r.getCode())
                            .type(r.getType())
                            .description(r.getDescription())
                            .build())
                    .toList();

        } catch (DataAccessException dae) {
            log.error("Database error while fetching roles", dae);
            throw new TradeIdentityException("Database error while fetching roles", dae);
        }
    }

    // ------------------ CREATE ROLE ------------------

    @Transactional
    public RoleResponseResponseDto createRole(CreateRoleRequestDto req) {
        log.info("Creating role code={} name={}", req != null ? req.getCode() : null, req != null ? req.getName() : null);

        if (req == null) {
            throw new IllegalArgumentException("Role request cannot be null");
        }
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new IllegalArgumentException("Role code is required");
        }
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }

        try {
            Role role = new Role();
            role.setCode(req.getCode().trim());
            role.setName(req.getName().trim());
            role.setDescription(req.getDescription());
            role.setType("CUSTOM");

            Role saved = roleRepo.save(role);

            log.info("Role created roleId={} code={}", saved.getId(), saved.getCode());

            return RoleResponseResponseDto.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .code(saved.getCode())
                    .type(saved.getType())
                    .description(saved.getDescription())
                    .build();

        } catch (DataIntegrityViolationException dive) {
            // duplicate role code, constraints, etc.
            log.error("Role create failed due to data integrity violation code={}", req.getCode(), dive);
            throw new TradeIdentityException("Role code already exists or invalid role data", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while creating role code={}", req.getCode(), dae);
            throw new TradeIdentityException("Database error while creating role", dae);
        }
    }

    // ------------------ DELETE ROLE ------------------

    @Transactional
    public RoleResponseResponseDto deleteRoleById(int roleId) {
        log.info("Deleting role roleId={}", roleId);

        if (roleId <= 0) {
            throw new IllegalArgumentException("Invalid roleId");
        }

        try {
            roleRepo.deleteById(roleId);
            log.info("Deleted role roleId={}", roleId);

            return RoleResponseResponseDto.builder()
                    .id(roleId)
                    .build();

        } catch (EmptyResultDataAccessException ex) {
            log.warn("Role not found for deletion roleId={}", roleId);
            throw new ResourceNotFoundException("Role not found with id: " + roleId);

        } catch (DataIntegrityViolationException dive) {
            // FK constraints: role is assigned to users
            log.error("Cannot delete role roleId={} due to integrity violation", roleId, dive);
            throw new TradeIdentityException("Role cannot be deleted because it is assigned to users", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while deleting role roleId={}", roleId, dae);
            throw new TradeIdentityException("Database error while deleting role", dae);
        }
    }

    // ------------------ UPDATE ROLE ------------------

    @Transactional
    public RoleResponseResponseDto updateRole(int roleId, UpdateRoleRequestDto roleReq) {
        log.info("Updating role roleId={}", roleId);

        if (roleId <= 0) {
            throw new IllegalArgumentException("Invalid roleId");
        }
        if (roleReq == null) {
            throw new IllegalArgumentException("Update request cannot be null");
        }

        try {
            Role role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

            if (roleReq.getName() != null && !roleReq.getName().isBlank()) {
                role.setName(roleReq.getName().trim());
            }
            if (roleReq.getCode() != null && !roleReq.getCode().isBlank()) {
                role.setCode(roleReq.getCode().trim()); // ✅ FIXED (you had name -> code)
            }
            if (roleReq.getDescription() != null) {
                role.setDescription(roleReq.getDescription());
            }

            Role saved = roleRepo.save(role);

            log.info("Updated role roleId={} code={}", saved.getId(), saved.getCode());

            return RoleResponseResponseDto.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .code(saved.getCode())
                    .type(saved.getType())
                    .description(saved.getDescription())
                    .build();

        } catch (DataIntegrityViolationException dive) {
            log.error("Role update failed due to data integrity violation roleId={}", roleId, dive);
            throw new TradeIdentityException("Role update violates constraints (duplicate code or invalid data)", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while updating role roleId={}", roleId, dae);
            throw new TradeIdentityException("Database error while updating role", dae);
        }
    }

    // ------------------ ASSIGN ROLES TO USER ------------------

    @Transactional
    public void createUserRoles(int userId, List<RoleResponseResponseDto> roles, Authentication auth) {
        int assignedBy = -1;
        if (auth != null && auth.getPrincipal() instanceof SecurityUser su) {
            assignedBy = su.userId();
        }

        log.info("Assigning {} roles to userId={} assignedBy={}",
                roles == null ? 0 : roles.size(), userId, assignedBy);

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (roles == null) {
            throw new IllegalArgumentException("Roles list cannot be null");
        }

        try {
            int deleted = userRoleRepo.deleteByUserId(userId); // recommend returning int
            log.debug("Deleted {} existing roles for userId={}", deleted, userId);

            for (RoleResponseResponseDto dto : roles) {
                if (dto == null || dto.getId() <= 0) {
                    throw new IllegalArgumentException("Invalid role in request");
                }

                Role role = roleRepo.findById(dto.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getId()));

                userRoleRepo.save(new UserRole(userId, role.getId(), assignedBy));
            }

            log.info("Roles assigned successfully userId={}", userId);

        } catch (DataIntegrityViolationException dive) {
            log.error("Role assignment failed due to integrity violation userId={}", userId, dive);
            throw new TradeIdentityException("Role assignment failed due to duplicate/invalid mapping", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while assigning roles userId={}", userId, dae);
            throw new TradeIdentityException("Database error while assigning roles", dae);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAllRolesByUserId(int id) {
        return roleCodesForUser(id);
    }

    // ------------------ DEFAULT ROLE ------------------

    @Transactional
    public void createDefaultRole(int userId) {
        log.info("Assigning default role to userId={}", userId);

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        try {
            Role role = roleRepo.findByCode("ROLE_TRADER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role not found: ROLE_TRADER"));

            userRoleRepo.save(new UserRole(userId, role.getId(), userId));

            log.info("Default role assigned userId={} roleId={}", userId, role.getId());

        } catch (DataIntegrityViolationException dive) {
            log.error("Default role assignment failed due to integrity violation userId={}", userId, dive);
            throw new TradeIdentityException("Default role already assigned or invalid mapping", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while assigning default role userId={}", userId, dae);
            throw new TradeIdentityException("Database error while assigning default role", dae);
        }
    }
}