package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.model.Role;
import com.company.dotaadminbackend.domain.role.dto.CreateRoleRequest;
import com.company.dotaadminbackend.domain.role.dto.UpdateRoleRequest;
import com.company.dotaadminbackend.infrastructure.adapter.RoleRepository;
import com.company.dotaadminbackend.infrastructure.entity.RoleEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

// RoleService
// ------------------------------------------------------------------
// Responsibilities:
//  - Pure CRUD for RoleEntity
//  - No seeding / no authority mapping logic here
// Seeding: bootstrap/RoleInitializer & bootstrap/UserInitializer
// Registration-time role decision: UserService.resolveRegistrationRole
// Keep comments ASCII to avoid encoding/BOM issues on some servers.
@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
    log.debug("[RoleService] Fetching all roles from repository");
    List<Role> list = roleRepository.findAll()
                .stream()
                .map(this::toRole)
                .collect(Collectors.toList());
    log.debug("[RoleService] Fetched {} roles", list.size());
    return list;
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    log.debug("[RoleService] Found role id={}", id);
        return toRole(entity);
    }

    public Role createRole(CreateRoleRequest request) {
        // Only validates uniqueness and persists. Seeding & fallbacks live elsewhere.
        if (roleRepository.existsByName(request.getName())) {
            String msg = "Role with name '" + request.getName() + "' already exists";
            log.warn("[RoleService] {}", msg);
            throw new RuntimeException(msg);
        }

        RoleEntity entity = new RoleEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        RoleEntity savedEntity = roleRepository.save(entity);
        log.debug("[RoleService] Created role id={}", savedEntity.getId());
        return toRole(savedEntity);
    }

    public Role updateRole(Long id, UpdateRoleRequest request) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Check if name is already taken by another role
        if (!entity.getName().equals(request.getName()) && roleRepository.existsByName(request.getName())) {
            String msg = "Role with name '" + request.getName() + "' already exists";
            log.warn("[RoleService] {}", msg);
            throw new RuntimeException(msg);
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        
    RoleEntity savedEntity = roleRepository.save(entity);
    log.debug("[RoleService] Updated role id={}", savedEntity.getId());
    return toRole(savedEntity);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            String msg = "Role not found with id: " + id;
            log.warn("[RoleService] {}", msg);
            throw new RuntimeException(msg);
        }
        roleRepository.deleteById(id);
        log.debug("[RoleService] Deleted role id={}", id);
    }

    private Role toRole(RoleEntity entity) {
        return new Role(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
